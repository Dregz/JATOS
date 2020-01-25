package services.gui;

import org.fest.assertions.Fail;
import org.junit.Test;
import utils.common.HashUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.fest.assertions.Assertions.assertThat;

/**
 * Tests HashUtils
 *
 * @author Kristian Lange
 */
public class HashUtilsTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }

    @Test
    public void checkGetHashMDFive() {
        String hash = null;
        try {
            hash = HashUtils.getHashMD5("bla");
        } catch (RuntimeException e) {
            Fail.fail();
        }
        assertThat(hash).isEqualTo("128ecf542a35ac5270a87dc740918404");
    }

    @Test
    public void checkGetHash() {
        String hash = null;
        try {
            hash = HashUtils.getHash("bla", "SHA-256");
        } catch (RuntimeException e) {
            Fail.fail();
        }
        assertThat(hash).isEqualTo("4df3c3f68fcc83b27e9d42c90431a72499f17875c81a599b566c9889b9696703");
    }

    @Test
    public void checkGetHashEmptyString() {
        String hash = null;
        try {
            hash = HashUtils.getHash("", "SHA-256");
        } catch (RuntimeException e) {
            Fail.fail();
        }
        assertThat(hash).isEqualTo("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855");
    }

    @Test
    public void checkGetFileHash() {
        String hash = null;
        try {
            Path file = Paths.get("test/resources/example.png");
            hash = HashUtils.getHash(file, "SHA-256");
        } catch (RuntimeException | IOException e) {
            Fail.fail();
        }
        assertThat(hash).isEqualTo("cfdd7ac0188d16640e05861b2df8e5881831cd2adcdd836b5bbca15791d40686");
    }
}
