/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.network.mediaserver.jupnp.support.contentdirectory.result.namespace.didl_lite.container;

import net.pms.network.mediaserver.jupnp.support.contentdirectory.result.namespace.didl_lite.DIDL_LITE;
import net.pms.network.mediaserver.jupnp.support.contentdirectory.result.namespace.upnp.StorageMediumValue;
import net.pms.network.mediaserver.jupnp.support.contentdirectory.result.namespace.upnp.UPNP;

/**
 * A storageVolume instance represents all, or a partition of, some physical
 * storage unit of a single type (as indicated by the storageMedium property).
 *
 * The storageVolume container may be writable, indicating whether new items can
 * be created as children of the storageVolume container. A storageVolume
 * container may contain other objects, except a storageSystem container or
 * another storageVolume container. A storageVolume container shall either be a
 * child of the root container or a child of a storageSystem container. This
 * class is derived from the container class and inherits the properties defined
 * by that class.
 */
public class StorageVolume extends Container {

	private static final UPNP.Class CLASS = new UPNP.Class(DIDL_LITE.OBJECT_CONTAINER_STORAGEVOLUME_TYPE);

	public StorageVolume() {
		setUpnpClass(CLASS);
	}

	public StorageVolume(Container other) {
		super(other);
		setUpnpClass(CLASS);
	}

	public StorageVolume(String id, Container parent, String title, String creator, Long childCount,
			Long storageTotal, Long storageUsed, Long storageFree, StorageMediumValue storageMedium) {
		this(id, parent.getId(), title, creator, childCount, storageTotal, storageUsed, storageFree, storageMedium);
	}

	public StorageVolume(String id, String parentID, String title, String creator, Long childCount,
			Long storageTotal, Long storageUsed, Long storageFree, StorageMediumValue storageMedium) {
		super(id, parentID, title, creator, CLASS, childCount);
		if (storageTotal != null) {
			setStorageTotal(storageTotal);
		}
		if (storageUsed != null) {
			setStorageUsed(storageUsed);
		}
		if (storageFree != null) {
			setStorageFree(storageFree);
		}
		if (storageMedium != null) {
			setStorageMedium(storageMedium);
		}
	}

	public Long getStorageTotal() {
		return properties.getValue(UPNP.StorageTotal.class);
	}

	public final StorageVolume setStorageTotal(Long l) {
		properties.set(new UPNP.StorageTotal(l));
		return this;
	}

	public Long getStorageUsed() {
		return properties.getValue(UPNP.StorageUsed.class);
	}

	public final StorageVolume setStorageUsed(Long l) {
		properties.set(new UPNP.StorageUsed(l));
		return this;
	}

	public Long getStorageFree() {
		return properties.getValue(UPNP.StorageFree.class);
	}

	public final StorageVolume setStorageFree(Long l) {
		properties.set(new UPNP.StorageFree(l));
		return this;
	}

	public String getStorageMedium() {
		return properties.getValue(UPNP.StorageMedium.class);
	}

	public StorageVolume setStorageMedium(String storageMedium) {
		properties.set(new UPNP.StorageMedium(storageMedium));
		return this;
	}

	public final StorageVolume setStorageMedium(StorageMediumValue storageMedium) {
		properties.set(new UPNP.StorageMedium(storageMedium));
		return this;
	}

	/**
	 * Check required properties.
	 */
	@Override
	public boolean isValid() {
		return super.isValid() &&
			properties.hasProperty(UPNP.StorageTotal.class) &&
			properties.hasProperty(UPNP.StorageUsed.class) &&
			properties.hasProperty(UPNP.StorageFree.class) &&
			properties.hasProperty(UPNP.StorageMedium.class);
	}
}