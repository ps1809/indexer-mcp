package com.projectiq.mcp.client.dto;

/**
 * DTO representing a method parameter from ProjectIQ Indexer.
 */
public class MethodParameter {

    private String name;
    private String type;

    public MethodParameter() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "MethodParameter{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}