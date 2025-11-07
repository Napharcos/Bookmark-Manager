package org.napharcos.bookmarkmanager.container

import org.napharcos.bookmarkmanager.database.BrowserDBManager

class ContainerImpl() : Container {

    override val browserDatabase by lazy { BrowserDBManager() }

    override val serverDatabase by lazy { null }
}