package fitnesse.http;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class MockRequestBuilderTest {
    @Test
    public void aCommandWithASlashShouldGenerateARequestResourceWithoutASlash() {
        assertThat(requestFrom("/myCommand").getResource(), is("myCommand"));
    }

    @Test
    public void aCommandWithoutASlashShouldGenerateARequestResourceWithoutASlash() {
        assertThat(requestFrom("myCommand").getResource(), is("myCommand"));
    }

    @Test
    public void aCommandShouldBeCorrectlyDeducedWhenCredentialsAreSpecified() {
        Request request = requestFrom("user:pass:/myCommand");
        assertThat(request.getResource(), is("myCommand"));
    }

    @Test
    public void requestAuthenticationShouldBeCorrectlySetWhenCredentialsAreSpecified() {
        Request request = requestFrom("user:pass:/myCommand");
        assertThat(request.getAuthorizationUsername(), is("user"));
        assertThat(request.getAuthorizationPassword(), is("pass"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void aSpecificationShouldBeWellFormed() {
        requestFrom("abc:/myCommand");
    }

    protected Request requestFrom(String commandSpecification) {
        return new MockRequestBuilder(commandSpecification).build();
    }
}
