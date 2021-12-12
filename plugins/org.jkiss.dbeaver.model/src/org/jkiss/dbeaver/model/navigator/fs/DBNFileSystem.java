/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.model.navigator.fs;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.model.DBConstants;
import org.jkiss.dbeaver.model.DBIcon;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.model.DBPImage;
import org.jkiss.dbeaver.model.fs.DBFVirtualFileSystem;
import org.jkiss.dbeaver.model.meta.Property;
import org.jkiss.dbeaver.model.navigator.DBNEvent;
import org.jkiss.dbeaver.model.navigator.DBNLazyNode;
import org.jkiss.dbeaver.model.navigator.DBNNode;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;

import java.nio.file.Path;
import java.util.*;

/**
 * DBNFileSystem
 */
public class DBNFileSystem extends DBNNode implements DBNLazyNode
{
    private static final Log log = Log.getLog(DBNFileSystem.class);

    private DBFVirtualFileSystem fileSystem;
    private DBNPath[] children;

    public DBNFileSystem(DBNNode parentNode, DBFVirtualFileSystem fileSystem) {
        super(parentNode);
        this.fileSystem = fileSystem;
    }

    @Override
    public boolean isDisposed() {
        return fileSystem == null || super.isDisposed();
    }

    @Override
    protected void dispose(boolean reflect) {
        children = null;
        this.fileSystem = null;
        super.dispose(reflect);
    }

    @Override
    public String getNodeType() {
        return "FileSystem";
    }

    @Override
    @Property(id = DBConstants.PROP_ID_NAME, viewable = true, order = 1)
    public String getNodeName() {
        return fileSystem.getFileSystemDisplayName();
    }

    @Override
//    @Property(viewable = false, order = 100)
    public String getNodeDescription() {
        return fileSystem.getDescription();
    }

    @Override
    public DBPImage getNodeIcon() {
        return DBIcon.TREE_FOLDER;
    }

    @Override
    public boolean allowsChildren() {
        return true;
    }

    @Override
    public DBNPath[] getChildren(DBRProgressMonitor monitor) throws DBException {
        if (children == null) {
            this.children = readChildNodes(monitor);
        }
        return children;
    }

    protected DBNPath[] readChildNodes(DBRProgressMonitor monitor) throws DBException {
        List<DBNPath> result = new ArrayList<>();
        for (Path rootPath : fileSystem.getFileSystem(monitor).getRootDirectories()) {
            result.add(new DBNPath(this, rootPath));
        };
        if (result.isEmpty()) {
            return new DBNPath[0];
        } else {
            final DBNPath[] childNodes = result.toArray(new DBNPath[0]);
            sortChildren(childNodes);
            return childNodes;
        }
    }

    @Override
    public boolean isManagable() {
        return true;
    }

    @Override
    public DBNNode refreshNode(DBRProgressMonitor monitor, Object source) throws DBException {
        children = null;
        return this;
    }

    @Override
    public String getNodeItemPath() {
        return getParentNode().getNodeItemPath() + fileSystem.getId();
    }

    @Override
    public boolean supportsRename() {
        return false;
    }

    protected void sortChildren(DBNNode[] list) {
        Arrays.sort(list, (o1, o2) -> {
            return o1.getNodeName().compareToIgnoreCase(o2.getNodeName());
        });
    }

    public Collection<DBPDataSourceContainer> getAssociatedDataSources() {
        return Collections.emptyList();
    }

    public void refreshResourceState(Object source) {
        //path.
        fireNodeEvent(new DBNEvent(source, DBNEvent.Action.UPDATE, this));
    }

    @Override
    public <T> T getAdapter(Class<T> adapter) {
        if (fileSystem != null && adapter.isAssignableFrom(fileSystem.getClass())) {
            return adapter.cast(fileSystem);
        }
        return super.getAdapter(adapter);
    }

    @Override
    public String toString() {
        return fileSystem.getFileSystemDisplayName();
    }

    @Override
    public boolean needsInitialization() {
        return children == null;
    }
}
