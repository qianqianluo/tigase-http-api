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
package tigase.http;

import tigase.http.modules.Module;
import tigase.server.Packet;
import tigase.xmpp.jid.JID;

public interface PacketWriter {

	boolean isAdmin(JID user);

	public boolean write(Module module, Packet packet);

	public boolean write(Module module, Packet packet, Integer timeout, Callback callback);

	public static interface Callback {

		public void onResult(Packet packet);

	}

}
