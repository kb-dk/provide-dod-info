package dk.kb.provide_dod_info.exception;

import dk.kb.provide_dod_info.testutils.TestFileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class ArgumentCheckTest extends ExtendedTestCase {

    @AfterClass
    public void tearDown() {
        TestFileUtils.tearDown();
    }

    @Test
    public void testConstructorWithoutCause() {
        addDescription("Test the constructor without the cause");
        String reason = "REASON: " + UUID.randomUUID().toString();
        Exception e = new ArgumentCheck(reason);
        assertEquals(e.getMessage(), reason);
        assertNull(e.getCause());
    }

    @Test
    public void testConstructorWithCause() {
        addDescription("Test the constructor with the cause");
        String reason = "REASON: " + UUID.randomUUID().toString();
        Exception cause = new Exception("cause");
        Exception e = new ArgumentCheck(reason, cause);
        assertEquals(e.getMessage(), reason);
        assertEquals(e.getCause(), cause);
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNullObjectFailure() {
        addDescription("Test that the null test fails with a null");
        ArgumentCheck.checkNotNull(null, "TEST");
    }

    @Test
    public void testNullObjectSuccess() {
        addDescription("Test the null test on a non-null object");
        ArgumentCheck.checkNotNull(new Object(), "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNullOrEmptyStringEmptyFailure() {
        addDescription("Test that the string test fails with an empty string");
        ArgumentCheck.checkNotNullOrEmpty("", "TEST");
    }

    @Test
    public void testNullOrEmptyStringEmptySuccess() {
        addDescription("Test the string test on an non-empty string");
        ArgumentCheck.checkNotNullOrEmpty("NON EMPTY STRING", "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNullOrEmptyByteArrayEmptyFailure() {
        addDescription("Test that the byte array test fails on an empty byte array");
        ArgumentCheck.checkNotNullOrEmpty(new byte[0], "TEST");
    }

    @Test
    public void testNullOrEmptyByteArraySuccess() {
        addDescription("Test the byte array test on an non-empty byte array");
        ArgumentCheck.checkNotNullOrEmpty("GNU".getBytes(StandardCharsets.UTF_8), "TEST"); //dahe
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNullOrEmptyMapEmptyFailure() {
        addDescription("Test that the map test fails on an empty map");
        ArgumentCheck.checkNotNullOrEmpty(new HashMap<String, String>(), "TEST");
    }

    @Test
    public void testNullOrEmptyMapSuccess() {
        addDescription("Test the map test on an non-empty map");
        Map<String, String> map = new HashMap<>();
        map.put("TEST", "TEST");
        ArgumentCheck.checkNotNullOrEmpty(map, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNonNegativeIntFailureNegative() {
        addDescription("Test that the non-negative integer test fails on negative numbers");
        ArgumentCheck.checkNotNegativeInt(-1, "TEST");
    }

    @Test
    public void testNonNegativeIntSuccessZero() {
        addDescription("Test the non-negative integer test on zero");
        ArgumentCheck.checkNotNegativeInt(0, "TEST");
    }

    @Test
    public void testNonNegativeIntSuccess() {
        addDescription("Test the non-negative integer test on positive numbers");
        ArgumentCheck.checkNotNegativeInt(1, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testPositiveIntFailureNegative() {
        addDescription("Test that the positive integer test fails on negative numbers");
        ArgumentCheck.checkPositiveInt(-1, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testPositiveIntFailureZero() {
        addDescription("Test that the positive integer test fails on zero");
        ArgumentCheck.checkPositiveInt(0, "TEST");
    }

    @Test
    public void testPositiveIntSuccess() {
        addDescription("Test the positive integer test on positive numbers");
        ArgumentCheck.checkPositiveInt(1, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNonNegativeLongFailureNegative() {
        addDescription("Test that the non-negative long test fails on negative numbers");
        ArgumentCheck.checkNotNegativeLong(-1L, "TEST");
    }

    @Test
    public void testNonNegativeLongSuccessZero() {
        addDescription("Test the non-negative long test on zero");
        ArgumentCheck.checkNotNegativeLong(0L, "TEST");
    }

    @Test
    public void testNonNegativeLongSuccess() {
        addDescription("Test the non-negative long test on positive numbers");
        ArgumentCheck.checkNotNegativeLong(1L, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testPositiveLongFailureNegative() {
        addDescription("Test that the positive long test fails on negative numbers");
        ArgumentCheck.checkPositiveLong(-1L, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testPositiveLongFailureZero() {
        addDescription("Test that the positive long test fails on zero");
        ArgumentCheck.checkPositiveLong(0L, "TEST");
    }

    @Test
    public void testPositiveLongSuccess() {
        addDescription("Test the positive long test on positive numbers");
        ArgumentCheck.checkPositiveLong(1L, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testNullOrEmptyCollectionFailure() {
        addDescription("Test that the null or empty collection test fails on an empty collection");
        ArgumentCheck.checkNotNullOrEmpty(new HashSet<>(), "TEST");
    }

    @Test
    public void testNullOrEmptyCollectionSuccess() {
        addDescription("Test the null or empty collection test on an non-empty collection");
        ArgumentCheck.checkNotNullOrEmpty(Collections.singletonList(new Object()), "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testTrueFailure() {
        addDescription("Test that the true test fails on a false");
        ArgumentCheck.checkTrue(false, "TEST");
    }

    @Test
    public void testTrueSuccess() {
        addDescription("Test the true test on a true");
        ArgumentCheck.checkTrue(true, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testDirectoryFailureMissing() {
        addDescription("Test that the directory test fails on a non-existing file/directory");
        File f = new File(UUID.randomUUID().toString());
        assertFalse(f.exists());
        ArgumentCheck.checkExistsDirectory(f, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testDirectoryFailureFile() {
        addDescription("Test that the directory test fails on a file");
        File f = new File("README.md");
        assertTrue(f.isFile());
        ArgumentCheck.checkExistsDirectory(f, "TEST");
    }

    @Test
    public void testDirectorySuccess() {
        addDescription("Test the directory test on a directory");
        TestFileUtils.setup();
        assertTrue(TestFileUtils.getTempDir().isDirectory());
        ArgumentCheck.checkExistsDirectory(TestFileUtils.getTempDir(), "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testFileFailureMissing() {
        addDescription("Test that the file test fails on a non-existing file/directory");
        File f = new File(UUID.randomUUID().toString());
        assertFalse(f.exists());
        ArgumentCheck.checkExistsNormalFile(f, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testFileFailureDirectory() {
        addDescription("Test that the file test fails on a directory");
        TestFileUtils.setup();
        assertTrue(TestFileUtils.getTempDir().isDirectory());
        ArgumentCheck.checkExistsNormalFile(TestFileUtils.getTempDir(), "TEST");
    }

    @Test
    public void testFileSuccess() {
        addDescription("Test the file test on a file");
        File f = new File("README.md");
        assertTrue(f.isFile());
        ArgumentCheck.checkExistsNormalFile(f, "TEST");
    }

    @Test
    public void testCheckThatMapContainsKeySuccess() {
        addDescription("Test the checkThatMapContainsKey method on a map that contains the key");
        String keyName = "TEST";
        Map<String, String> map = new HashMap<>();
        map.put(keyName, UUID.randomUUID().toString());
        ArgumentCheck.checkThatMapContainsKey(map, keyName, "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testCheckThatMapContainsKeyFailureNoKey() {
        addDescription("Test the checkThatMapContainsKey method on a map that does not contains the given key");
        String keyName = "TEST";
        Map<String, String> map = new HashMap<>();
        map.put(keyName, UUID.randomUUID().toString());
        ArgumentCheck.checkThatMapContainsKey(map, UUID.randomUUID().toString(), "TEST");
    }

    @Test(expectedExceptions = ArgumentCheck.class)
    public void testCheckThatMapContainsKeyFailureNullValue() {
        addDescription("Test the checkThatMapContainsKey method on a map that contains a null for the given key");
        String keyName = "TEST";
        Map<String, String> map = new HashMap<>();
        map.put(keyName, null);
        ArgumentCheck.checkThatMapContainsKey(map, keyName, "TEST");
    }

}
