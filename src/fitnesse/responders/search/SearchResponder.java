// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.TitleWikiPageFinder;
import fitnesse.components.TraversalListener;

import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.LITERAL;

public class SearchResponder extends ResultResponder {

    private String getSearchString() {
        return (String) request.getInput("searchString");
    }

    private String getSearchType() {
        String searchType = (String) request.getInput("searchType");

        if (searchType == null || searchType.toLowerCase().contains("title"))
            return "Title";
        else
            return "Content";
    }

    protected String getTitle() {
        return getSearchType() + " Search Results for '" + getSearchString() + "'";
    }

    @Override
    public void traverse(TraversalListener<Object> observer) {
        String searchString = getSearchString();
        if (!"".equals(searchString)) {
            String searchType = getSearchType();
            if ("Title".equals(searchType))
                new TitleWikiPageFinder(searchString, observer).search(root);
            else {
                Pattern regularExpression = Pattern.compile(searchString, CASE_INSENSITIVE + LITERAL);
                new RegularExpressionWikiPageFinder(regularExpression, observer).search(root);
            }
        }
    }

    protected boolean shouldRespondWith404() {
        return false;
    }

}
