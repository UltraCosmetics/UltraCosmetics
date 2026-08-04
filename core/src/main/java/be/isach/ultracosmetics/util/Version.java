package be.isach.ultracosmetics.util;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Version implements Comparable<Version> {

    // only numbers (ex. 2.6.1)
    private final String version;
    // Classifier only (ex. RELEASE)
    private final String classifier;
    private final String gitHash;


    public Version(String version, String gitHash) {
        if (version == null) {
            throw new IllegalArgumentException("Version can not be null");
        }
        this.version = findVersion(version);
        this.classifier = findClassifier(version);
        this.gitHash = gitHash;
    }

    public Version(String version) {
        this(version, null);
    }

    private static String findVersion(String version) {
        Matcher matcher = Pattern.compile("\\d+(?:\\.\\d+)+").matcher(version);
        matcher.find();
        return matcher.group();
    }

    private static String findClassifier(String version) {
        if (!version.contains("-")) {
            return "RELEASE";
        }
        return version.split("-", 2)[1];
    }

    public final String numbersOnly() {
        return this.version;
    }

    public String versionWithClassifier() {
        return version + "-" + classifier;
    }

    public String versionClassifierCommit() {
        StringBuilder builder = new StringBuilder(version);
        builder.append('-').append(classifier);
        if (gitHash != null) {
            builder.append(" (commit ").append(gitHash).append(')');
        }
        return builder.toString();
    }

    @Override
    public int compareTo(Version otherVersion) {
        String[] thisParts = this.numbersOnly().split("\\.");
        String[] thatParts = otherVersion.numbersOnly().split("\\.");
        int length = Math.max(thisParts.length, thatParts.length);
        for (int i = 0; i < length; i++) {
            int thisPart = i < thisParts.length ?
                    Integer.parseInt(thisParts[i]) : 0;
            int thatPart = i < thatParts.length ?
                    Integer.parseInt(thatParts[i]) : 0;
            int cmp = Integer.compare(thisPart, thatPart);
            if (cmp != 0) {
                return cmp;
            }
        }
        // release > dev build of same version
        return Boolean.compare(this.isRelease(), otherVersion.isRelease());
    }

    public boolean isDev() {
        return classifier.toLowerCase(Locale.ROOT).contains("dev");
    }

    public boolean isRelease() {
        return !isDev();
    }

    @Override
    public boolean equals(Object that) {
        return this == that ||
                that != null && this.getClass() == that.getClass() && this.compareTo((Version) that) == 0;
    }

    @Override
    public int hashCode() {
        return version.hashCode();
    }

    @Override
    public String toString() {
        return numbersOnly();
    }
}
