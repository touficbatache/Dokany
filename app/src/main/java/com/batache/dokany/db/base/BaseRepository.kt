package com.batache.dokany.db.base

import com.batache.dokany.api.FirebaseRepository

open class BaseRepository {
  /**
   * Firebase repository
   */
  protected val firebaseRepo by lazy { FirebaseRepository() }
}
