/*
 * Service.java
 *
 * Tigase HTTP API
 * Copyright (C) 2004-2017 "Tigase, Inc." <office@tigase.com>
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
package tigase.http.api;

import groovy.lang.Closure;
import tigase.db.AuthRepository;
import tigase.db.AuthorizationException;
import tigase.db.TigaseDBException;
import tigase.db.UserRepository;
import tigase.http.PacketWriter.Callback;
import tigase.http.modules.Module;
import tigase.server.Packet;
import tigase.util.stringprep.TigaseStringprepException;
import tigase.xmpp.jid.BareJID;

public interface Service<T extends Module> {
	
	void sendPacket(Packet packet, Long timeout, Callback closure);
    void sendPacket(Packet packet, Long timeout, Closure closure);
    UserRepository getUserRepository();
    AuthRepository getAuthRepository();
    boolean isAdmin(BareJID user);
	boolean isAllowed(String key, String domain, String path);
	boolean checkCredentials(String user, String password) throws TigaseStringprepException, TigaseDBException, AuthorizationException;

	T getModule();
}
