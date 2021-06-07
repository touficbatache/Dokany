package com.batache.dokany.app.favorites

import androidx.lifecycle.*
import com.batache.dokany.DokanyApplication
import com.batache.dokany.app.base.BaseViewModel
import com.batache.dokany.model.pojo.product.Product
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.launch

class FavoritesViewModel(application: DokanyApplication) : BaseViewModel(application) {

  /**
   * The data source this ViewModel will fetch results from.
   */
  private val repository = application.productsRepository

  /**
   * Live data.
   */
  val productsInFavorites: LiveData<List<Product>> = repository.productsInFavorites

  init {
    refreshDataFromRepository()
  }

  private fun refreshDataFromRepository() {
    viewModelScope.launch {
      try {
        // TODO: Make refresh favorites work
//        repository.refreshFavorites()
        _eventFirestoreError.value = false
        _isFirestoreErrorShown.value = false

      } catch (networkError: FirebaseFirestoreException) {
        // Show a Toast error message and hide the progress bar.
        if (productsInFavorites.value.isNullOrEmpty())
          _eventFirestoreError.value = true
      }
    }
  }

  /**
   * Resets the network error flag.
   */
  fun onFirestoreErrorShown() {
    _isFirestoreErrorShown.value = true
  }

  class Factory(val app: DokanyApplication) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
      if (modelClass.isAssignableFrom(FavoritesViewModel::class.java)) {
        @Suppress("UNCHECKED_CAST")
        return FavoritesViewModel(app) as T
      }
      throw IllegalArgumentException("Unable to construct ViewModel")
    }
  }
}