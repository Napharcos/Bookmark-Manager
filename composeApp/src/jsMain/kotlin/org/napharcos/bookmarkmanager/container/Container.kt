package org.napharcos.bookmarkmanager.container

import org.napharcos.bookmarkmanager.database.DatabaseRepository

interface Container {

    val browserDatabase: DatabaseRepository
}