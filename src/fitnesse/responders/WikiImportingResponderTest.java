// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.http.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import util.RegexTestCase;

import java.io.IOException;

public class WikiImportingResponderTest extends RegexTestCase {
    private WikiImportingResponder responder;
    private String baseUrl;
    private WikiImporterTest testData;

    public void setUp() throws Exception {
        testData = new WikiImporterTest();
        testData.createRemoteRoot();
        testData.createLocalRoot();

        FitNesseUtil.startFitnesse(testData.remoteRoot);
        baseUrl = "http://localhost:" + FitNesseUtil.PORT + "/";

        createResponder();
    }

    private void createResponder() throws Exception {
        responder = new WikiImportingResponder();
        responder.path = new WikiPagePath();
        ChunkedResponse response = new ChunkedResponse("html", new MockChunkedDataProvider());
        response.sendTo(new MockResponseSender());
        responder.setResponse(response);
        responder.getImporter().setDeleteOrphanOption(false);
    }

    public void tearDown() throws Exception {
        FitNesseUtil.stopFitnesse();
    }

    public void testActionsOfMakeResponse() throws Exception {
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        assertEquals(2, testData.pageTwo.getChildren().size());
        WikiPage importedPageOne = testData.pageTwo.getChildPage("PageOne");
        assertNotNull(importedPageOne);
        assertEquals("page one", importedPageOne.getData().getContent());

        WikiPage importedPageTwo = testData.pageTwo.getChildPage("PageTwo");
        assertNotNull(importedPageTwo);
        assertEquals("page two", importedPageTwo.getData().getContent());

        assertEquals(1, importedPageOne.getChildren().size());
        WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
        assertNotNull(importedChildOne);
        assertEquals("child one", importedChildOne.getData().getContent());
    }

    public void testImportingFromNonRootPageUpdatesPageContent() throws Exception {
        PageData data = testData.pageTwo.getData();
        WikiImportProperty importProperty = new WikiImportProperty(baseUrl + "PageOne");
        importProperty.addTo(data.getProperties());
        data.setContent("nonsense");
        testData.pageTwo.commit(data);

        Response response = makeSampleResponse("blah");
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        data = testData.pageTwo.getData();
        assertEquals("page one", data.getContent());

        assertFalse(WikiImportProperty.createFrom(data.getProperties()).isRoot());
    }

    public void testImportPropertiesGetAdded() throws Exception {
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        checkProperties(testData.pageTwo, baseUrl, true, null);

        WikiPage importedPageOne = testData.pageTwo.getChildPage("PageOne");
        checkProperties(importedPageOne, baseUrl + "PageOne", false, testData.remoteRoot.getChildPage("PageOne"));

        WikiPage importedPageTwo = testData.pageTwo.getChildPage("PageTwo");
        checkProperties(importedPageTwo, baseUrl + "PageTwo", false, testData.remoteRoot.getChildPage("PageTwo"));

        WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
        checkProperties(importedChildOne, baseUrl + "PageOne.ChildOne", false, testData.remoteRoot.getChildPage("PageOne").getChildPage("ChildOne"));
    }

    private void checkProperties(WikiPage page, String source, boolean isRoot, WikiPage remotePage) throws Exception {
        WikiPageProperties props = page.getData().getProperties();
        if (!isRoot)
            assertFalse("should not have Edit property", props.has("Edit"));

        WikiImportProperty importProperty = WikiImportProperty.createFrom(props);
        assertNotNull(importProperty);
        assertEquals(source, importProperty.getSourceUrl());
        assertEquals(isRoot, importProperty.isRoot());

        if (remotePage != null) {
            long remoteLastModificationTime = remotePage.getData().getProperties().getLastModificationTime().getTime();
            long importPropertyLastModificationTime = importProperty.getLastRemoteModificationTime().getTime();
            assertEquals(remoteLastModificationTime, importPropertyLastModificationTime);
        }
    }

    private String simulateWebRequest(MockRequest request) throws IOException {
        ChunkedResponse response = getResponse(request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        return content;
    }

    public void testHtmlOfMakeResponse() throws IOException {
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        ((ChunkedResponse) response).turnOffChunking();
        sender.doSending(response);
        String content = sender.sentData();

        System.out.println(content);

        assertSubString("<html>", content);
        assertSubString("Wiki Import", content);

        assertSubString("PageTwo", content);
        assertSubString("PageTwo.PageOne", content);
        assertSubString("PageTwo.PageOne.ChildOne", content);
        assertSubString("Import complete.", content);
        assertSubString("3 pages were imported.", content);
    }

    public void testHtmlOfMakeResponseWithNoModifications() throws Exception {
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        // import a second time... nothing was modified
        createResponder();
        response = makeSampleResponse(baseUrl);
        sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertSubString("<html>", content);
        assertSubString("Wiki Import", content);

        assertSubString("PageTwo", content);
        assertNotSubString("PageTwo.PageOne", content);
        assertNotSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
        assertNotSubString("href=\"PageTwo.PageTwo\"", content);
        assertSubString("Import complete.", content);
        assertSubString("0 pages were imported.", content);
        assertSubString("3 pages were unmodified.", content);
    }

    private ChunkedResponse makeSampleResponse(String remoteUrl) {
        MockRequest request = makeRequest(remoteUrl);

        return getResponse(request);
    }

    private ChunkedResponse getResponse(MockRequest request) {
        ChunkedResponse response = (ChunkedResponse) responder.makeResponse(FitNesseUtil.makeTestContext(testData.localRoot), request);
        response.turnOffChunking();
        return response;
    }

    private MockRequest makeRequest(String remoteUrl) {
        MockRequest request = new MockRequest();
        request.setResource("PageTwo");
        request.addInput("responder", "import");
        request.addInput("remoteUrl", remoteUrl);
        return request;
    }

    public void testMakeResponseImportingNonRootPage() throws Exception {
        MockRequest request = makeRequest(baseUrl + "PageOne");

        Response response = responder.makeResponse(FitNesseUtil.makeTestContext(testData.localRoot), request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertNotNull(testData.pageTwo.getChildPage("ChildOne"));
        assertSubString("PageTwo.ChildOne", content);
        assertSubString("ChildOne", content);
    }

    public void testRemoteUrlNotFound() throws Exception {
        String remoteUrl = baseUrl + "PageDoesntExist";
        Response response = makeSampleResponse(remoteUrl);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
    }

    public void testErrorMessageForBadUrlProvided() throws Exception {
        String remoteUrl = baseUrl + "blah";
        Response response = makeSampleResponse(remoteUrl);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The URL's resource path, blah, is not a valid WikiWord.", content);
    }


    public void testListOfOrphanedPages() throws Exception {
        WikiImporter importer = new WikiImporter();

        responder.setImporter(importer);

        MockRequest request = makeRequest(baseUrl);
        String content = simulateWebRequest(request);

        assertNotSubString("orphan", content);
        //assertNotSubString("PageOne", content);
        //assertNotSubString("PageOne.ChildPagae", content);

        importer.getOrphans().add(new WikiPagePath(testData.pageOne));
        importer.getOrphans().add(new WikiPagePath(testData.childPageOne));

        content = simulateWebRequest(request);

        assertSubString("2 orphaned pages were found and have been removed.", content);
        assertSubString("PageOne", content);
        assertSubString("PageOne.ChildOne", content);
    }

    public void testAutoUpdatingTurnedOn() throws Exception {
        MockRequest request = makeRequest(baseUrl);
        responder.setRequest(request);
        responder.data = new PageData(new WikiPageDummy());

        responder.initializeImporter();
        assertFalse(responder.getImporter().getAutoUpdateSetting());

        request.addInput("autoUpdate", "1");
        responder.initializeImporter();
        assertTrue(responder.getImporter().getAutoUpdateSetting());
    }

    public void testAutoUpdateSettingDisplayed() throws Exception {
        WikiImporter importer = new MockWikiImporter();

        responder.setImporter(importer);

        MockRequest request = makeRequest(baseUrl);
        request.addInput("autoUpdate", true);
        String content = simulateWebRequest(request);

        assertSubString("Automatic Update turned ON", content);

        request = makeRequest(baseUrl);
        content = simulateWebRequest(request);

        assertSubString("Automatic Update turned OFF", content);
    }
}
