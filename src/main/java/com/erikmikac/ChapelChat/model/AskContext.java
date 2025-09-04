package com.erikmikac.ChapelChat.model;

/**
 * Represents the context of a single "ask" request.
 * This record encapsulates all the relevant information needed to process the
 * request,
 * log it, and handle any potential issues like rate limiting or prompt
 * injection.
 *
 * @param askRequest      The original request from the user.
 * @param orgId           The ID of the church this request is for.
 * @param ip              The IP address of the client making the request.
 * @param userAgent       The user agent of the client.
 * @param prompt          The system prompt being used to generate the response.
 * @param profileChecksum The checksum of the church profile used for this
 *                        request.
 * @param requestId       A unique identifier for tracking this request through
 *                        the logs.
 *  * @param orgType       The type of orgnzation: Ministry or SMB.
 */
public record AskContext(
        AskRequest askRequest,
        String orgId,
        String ip,
        String userAgent,
        String prompt,
        String profileChecksum,
        String requestId, // for log tracability
        String orgType,
        String tenantId) {
}