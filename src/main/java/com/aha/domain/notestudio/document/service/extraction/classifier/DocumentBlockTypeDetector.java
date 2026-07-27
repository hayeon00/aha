package com.aha.domain.notestudio.document.service.extraction.classifier;

/**
 * Please explain the class!!!
 *
 * @author : rlagkdus
 * @filename : DocumentBlockTypeDetector
 * @since : 2026. 7. 9. 목요일
 */
public interface DocumentBlockTypeDetector {

    boolean supports(String text);

    ClassifiedBlockType detect(String text);

    int order();
}