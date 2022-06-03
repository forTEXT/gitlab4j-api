package org.gitlab4j.api;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.List;
import java.util.Optional;

import org.gitlab4j.api.Constants.SortOrder;
import org.gitlab4j.api.models.AccessLevel;
import org.gitlab4j.api.models.Project;
import org.gitlab4j.api.models.ProtectedTag;
import org.gitlab4j.api.models.Release;
import org.gitlab4j.api.models.Tag;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

@Category(IntegrationTest.class)
public class TestTagsApi extends AbstractIntegrationTest {

    private static final String TEST_TAG_NAME_1 = "test-tag-1";
    private static final String TEST_TAG_NAME_0 = "test-tag-0";
    private static final String TEST_TAG_WITH_SLASH = "env/test-tag";
    private static final String TEST_PROTECTED_TAG = "protected-tag";

    private static GitLabApi gitLabApi;
    private static Project testProject;

    public TestTagsApi() {
        super();
    }

    @BeforeClass
    public static void testSetup() {

        // Must setup the connection to the GitLab test server and get the test Project instance
        gitLabApi = baseTestSetup();
        testProject = getTestProject();

        deleteTestTags();

        if (testProject != null) {
            try {
                gitLabApi.getTagsApi().createTag(testProject, TEST_TAG_NAME_0, "master");
            } catch (Exception ignore) {}
        }
    }

    @AfterClass
    public static void tearDown() {
        deleteTestTags();
    }

    private static final void deleteTestTags() {

        if (testProject != null) {
            try {
                gitLabApi.getTagsApi().deleteTag(testProject, TEST_TAG_NAME_0);
            } catch (Exception ignore) {}

            try {
                gitLabApi.getTagsApi().deleteTag(testProject, TEST_TAG_NAME_1);
            } catch (Exception ignore) {}

            try {
                gitLabApi.getTagsApi().deleteTag(testProject, TEST_TAG_WITH_SLASH);
            } catch (Exception ignore) {}

            try {
                gitLabApi.getTagsApi().unprotectTag(testProject, TEST_PROTECTED_TAG);
            } catch (Exception ignore) {}

            try {
                gitLabApi.getTagsApi().deleteTag(testProject, TEST_PROTECTED_TAG);
            } catch (Exception ignore) {}
        }
    }

    @Before
    public void beforeMethod() {
        assumeTrue(testProject != null);
    }

    @Test
    public void testCreateAndDeleteTag() throws GitLabApiException {

        Tag testTag = gitLabApi.getTagsApi().createTag(testProject, TEST_TAG_NAME_1, "master");
        assertNotNull(testTag);
        assertEquals(TEST_TAG_NAME_1, testTag.getName());

        testTag = gitLabApi.getTagsApi().getTag(testProject, TEST_TAG_NAME_1);
        assertNotNull(testTag);
        assertEquals(TEST_TAG_NAME_1, testTag.getName());

        gitLabApi.getTagsApi().deleteTag(testProject, TEST_TAG_NAME_1);
        Optional<Tag> tag = gitLabApi.getTagsApi().getOptionalTag(testProject, TEST_TAG_NAME_1);
        assertFalse(tag.isPresent());

        List<Tag> tags = gitLabApi.getTagsApi().getTags(testProject);
        assertFalse(tags.stream().map(Tag::getName).anyMatch(s -> TEST_TAG_NAME_1.equals(s)));
    }

    @Test
    @Ignore("There is no more tags API endpoint to create a release for an existing tag")
    public void testCreateAndUpdateRelease() throws GitLabApiException {

        Tag testTag = gitLabApi.getTagsApi().createTag(testProject, TEST_TAG_NAME_1, "master");
        assertNotNull(testTag);
        assertEquals(TEST_TAG_NAME_1, testTag.getName());

        Release release = gitLabApi.getTagsApi().createRelease(testProject, TEST_TAG_NAME_1, "RELEASE NOTES");
        assertNotNull(release);

        testTag = gitLabApi.getTagsApi().getTag(testProject, TEST_TAG_NAME_1);
        assertNotNull(testTag);
        assertEquals(TEST_TAG_NAME_1, testTag.getName());
        assertEquals("RELEASE NOTES", testTag.getRelease().getDescription());

        release = gitLabApi.getTagsApi().updateRelease(testProject, TEST_TAG_NAME_1, "UPDATED RELEASE NOTES");
        assertNotNull(release);

        testTag = gitLabApi.getTagsApi().getTag(testProject, TEST_TAG_NAME_1);
        assertNotNull(testTag);
        assertEquals(TEST_TAG_NAME_1, testTag.getName());
        assertEquals("UPDATED RELEASE NOTES", testTag.getRelease().getDescription());

        gitLabApi.getTagsApi().deleteTag(testProject, TEST_TAG_NAME_1);
    }

    @Test
    public void testGetTags() throws GitLabApiException {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(testProject);
        assertNotNull(tags);
        assertTrue(tags.size() > 0);
        assertTrue(tags.stream().map(Tag::getName).anyMatch(s -> TEST_TAG_NAME_0.equals(s)));
    }

    @Test
    public void testGetTagsPager() throws GitLabApiException {
        Pager<Tag> tags = gitLabApi.getTagsApi().getTags(testProject, 100);
        assertNotNull(tags);
        assertTrue(tags.getTotalItems() > 0);
        assertTrue(tags.stream().map(Tag::getName).anyMatch(s -> TEST_TAG_NAME_0.equals(s)));
    }

    @Test
    public void testGetTagWithSpecialCharacersInTagName() throws GitLabApiException {
        Tag testTag = gitLabApi.getTagsApi().createTag(testProject, TEST_TAG_WITH_SLASH, "master");
        assertNotNull(testTag);
        assertEquals(TEST_TAG_WITH_SLASH, testTag.getName());

        testTag = gitLabApi.getTagsApi().getTag(testProject, TEST_TAG_WITH_SLASH);
        assertNotNull(testTag);
        assertEquals(TEST_TAG_WITH_SLASH, testTag.getName());
    }

    @Test
    public void testGetTagsInAscOrder() throws GitLabApiException {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(testProject, Constants.TagOrderBy.NAME, SortOrder.ASC, null);
        assertNotNull(tags);
        assertTrue(tags.size() > 1);
        assertTrue(tags.get(0).getName().compareTo(tags.get(1).getName()) < 0);
    }

    @Test
    public void testGetTagsInDescOrder() throws GitLabApiException {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(testProject, Constants.TagOrderBy.NAME, SortOrder.DESC, null);
        assertNotNull(tags);
        assertTrue(tags.size() > 1);
        assertTrue(tags.get(0).getName().compareTo(tags.get(1).getName()) > 0);
    }

    @Test
    public void testGetTagsSearch() throws GitLabApiException {
        List<Tag> tags = gitLabApi.getTagsApi().getTags(testProject);
        assertNotNull(tags);
        assertTrue(tags.size() > 0);

        String tagName = tags.get(0).getName();
        tags = gitLabApi.getTagsApi().getTags(testProject, null, null, tagName);
        assertNotNull(tags);
        assertTrue(tags.size() == 1);
        assertEquals(tagName, tags.get(0).getName());

        tags = gitLabApi.getTagsApi().getTags(testProject, null, null, "NOT_FOUND_TAG_NAME");
        assertNotNull(tags);
        assertTrue(tags.isEmpty());
    }

    @Test
    public void  testProtectedTags() throws GitLabApiException {

        Tag testTag = gitLabApi.getTagsApi().createTag(testProject, TEST_PROTECTED_TAG, "master");
        assertNotNull(testTag);
        assertEquals(TEST_PROTECTED_TAG, testTag.getName());

        ProtectedTag protectedTag = gitLabApi.getTagsApi().protectTag(testProject, TEST_PROTECTED_TAG, AccessLevel.DEVELOPER);
        assertEquals(TEST_PROTECTED_TAG, protectedTag.getName());

        List<ProtectedTag> tags = gitLabApi.getTagsApi().getProtectedTags(testProject);
        assertTrue(tags.stream().map(ProtectedTag::getName).anyMatch(s -> TEST_PROTECTED_TAG.equals(s)));

        Optional<ProtectedTag> optionalTag = gitLabApi.getTagsApi().getOptionalProtectedTag(testProject, TEST_PROTECTED_TAG);
        assertTrue(optionalTag.isPresent());
        assertEquals(TEST_PROTECTED_TAG, optionalTag.get().getName());

        gitLabApi.getTagsApi().unprotectTag(testProject, TEST_PROTECTED_TAG);
        assertEquals(TEST_PROTECTED_TAG, protectedTag.getName());

        tags = gitLabApi.getTagsApi().getProtectedTags(testProject);
        assertFalse(tags.stream().map(ProtectedTag::getName).anyMatch(s -> TEST_PROTECTED_TAG.equals(s)));
    }
}
