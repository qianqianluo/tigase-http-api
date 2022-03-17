/*
 * Tigase HTTP API component - Tigase HTTP API component
 * Copyright (C) 2013 Tigase, Inc. (office@tigase.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */
package tigase.http.jaxrs;

import java.lang.reflect.Method;

public enum HttpMethod {
	GET,
	PUT,
	POST,
	DELETE;

	public static HttpMethod valueOf(Method method) {
		if (method.getAnnotation(jakarta.ws.rs.GET.class) != null) {
			return HttpMethod.GET;
		}
		if (method.getAnnotation(jakarta.ws.rs.POST.class) != null) {
			return HttpMethod.POST;
		}
		if (method.getAnnotation(jakarta.ws.rs.PUT.class) != null) {
			return HttpMethod.PUT;
		}
		if (method.getAnnotation(jakarta.ws.rs.DELETE.class) != null) {
			return HttpMethod.DELETE;
		}
		return null;
	}

//	public boolean requestContainsBody() {
//		switch (this) {
//			case POST, PUT:
//				return true;
//			case GET, DELETE:
//				return
//		}
//	}
}