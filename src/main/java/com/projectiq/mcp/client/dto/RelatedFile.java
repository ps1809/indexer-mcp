package com.projectiq.mcp.client.dto;

import java.util.Objects;

/**
 * DTO representing a single related file entry returned from the Indexer.
 */
public class RelatedFile {

    private String fileName;
    private String filePath;
    private String fileType;
    private String relationshipType;
    private String associatedPackage;

    public RelatedFile() {
    }

    public RelatedFile(String fileName, String filePath, String fileType, String relationshipType, String associatedPackage) {
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileType = fileType;
        this.relationshipType = relationshipType;
        this.associatedPackage = associatedPackage;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getRelationshipType() {
        return relationshipType;
    }

    public void setRelationshipType(String relationshipType) {
        this.relationshipType = relationshipType;
    }

    public String getAssociatedPackage() {
        return associatedPackage;
    }

    public void setAssociatedPackage(String associatedPackage) {
        this.associatedPackage = associatedPackage;
    }

    @Override
    public String toString() {
        return "RelatedFile{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                ", fileType='" + fileType + '\'' +
                ", relationshipType='" + relationshipType + '\'' +
                ", associatedPackage='" + associatedPackage + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RelatedFile that = (RelatedFile) o;
        return Objects.equals(fileName, that.fileName) &&
                Objects.equals(filePath, that.filePath) &&
                Objects.equals(fileType, that.fileType) &&
                Objects.equals(relationshipType, that.relationshipType) &&
                Objects.equals(associatedPackage, that.associatedPackage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, filePath, fileType, relationshipType, associatedPackage);
    }
}