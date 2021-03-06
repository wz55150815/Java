/*
 * Copyright 2002-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web;


import java.net.URI;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.Nullable;


/**
 * {@link RuntimeException} that implements {@link ErrorResponse} to expose
 * an HTTP status, response headers, and a body formatted as an RFC 7808
 * {@link ProblemDetail}.
 *
 * <p>The exception can be used as is, or it can be extended as a more specific
 * exception that populates the {@link ProblemDetail#setType(URI) type} or
 * {@link ProblemDetail#setDetail(String) detail} fields, or potentially adds
 * other non-standard fields.
 *
 * @author Rossen Stoyanchev
 * @since 6.0
 */
@SuppressWarnings("serial")
public class ErrorResponseException extends NestedRuntimeException implements ErrorResponse {

	private final int status;

	private final HttpHeaders headers = new HttpHeaders();

	private final ProblemDetail body;


	/**
	 * Constructor with a well-known {@link HttpStatus}.
	 */
	public ErrorResponseException(HttpStatus status) {
		this(status, null);
	}

	/**
	 * Constructor with a well-known {@link HttpStatus} and an optional cause.
	 */
	public ErrorResponseException(HttpStatus status, @Nullable Throwable cause) {
		this(status.value(), cause);
	}

	/**
	 * Constructor that accepts any status value, possibly not resolvable as an
	 * {@link HttpStatus} enum, and an optional cause.
	 */
	public ErrorResponseException(int status, @Nullable Throwable cause) {
		this(status, ProblemDetail.forRawStatusCode(status), cause);
	}

	/**
	 * Constructor with a given {@link ProblemDetail} instance, possibly a
	 * subclass of {@code ProblemDetail} with extended fields.
	 */
	public ErrorResponseException(int status, ProblemDetail body, @Nullable Throwable cause) {
		super(null, cause);
		this.status = status;
		this.body = body;
	}


	@Override
	public int getRawStatusCode() {
		return this.status;
	}

	@Override
	public HttpHeaders getHeaders() {
		return this.headers;
	}

	/**
	 * Set the {@link ProblemDetail#setType(URI) type} field of the response body.
	 * @param type the problem type
	 */
	public void setType(URI type) {
		this.body.setType(type);
	}

	/**
	 * Set the {@link ProblemDetail#setTitle(String) title} field of the response body.
	 * @param title the problem title
	 */
	public void setTitle(@Nullable String title) {
		this.body.setTitle(title);
	}

	/**
	 * Set the {@link ProblemDetail#setDetail(String) detail} field of the response body.
	 * @param detail the problem detail
	 */
	public void setDetail(@Nullable String detail) {
		this.body.setDetail(detail);
	}

	/**
	 * Set the {@link ProblemDetail#setInstance(URI) instance} field of the response body.
	 * @param instance the problem instance
	 */
	public void setInstance(@Nullable URI instance) {
		this.body.setInstance(instance);
	}

	/**
	 * Return the body for the response. To customize the body content, use:
	 * <ul>
	 * <li>{@link #setType(URI)}
	 * <li>{@link #setTitle(String)}
	 * <li>{@link #setDetail(String)}
	 * <li>{@link #setInstance(URI)}
	 * </ul>
	 * <p>By default, the status field of {@link ProblemDetail} is initialized
	 * from the status provided to the constructor, which in turn may also
	 * initialize the title field from the status reason phrase, if the status
	 * is well-known. The instance field, if not set, is initialized from the
	 * request path when a {@code ProblemDetail} is returned from an
	 * {@code @ExceptionHandler} method.
	 */
	@Override
	public final ProblemDetail getBody() {
		return this.body;
	}

	@Override
	public String getMessage() {
		HttpStatus httpStatus = HttpStatus.resolve(this.status);
		String message = (httpStatus != null ? httpStatus : String.valueOf(this.status)) +
				(!this.headers.isEmpty() ? ", headers=" + this.headers : "") + ", " + this.body;
		return NestedExceptionUtils.buildMessage(message, getCause());
	}

}
