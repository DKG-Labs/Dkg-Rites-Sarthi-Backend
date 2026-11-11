package com.sarthi.udmcomplaint.dto;

/** Downloaded document content for streaming to the browser. */
public record UdmDocumentFile(String fileName, String contentType, byte[] content) { }
