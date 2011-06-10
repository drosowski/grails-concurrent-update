package com.rosowski.grails.plugin

@ConcurrentUpdate
/**
 * Class used only to test OptimisticLock annotation. Excluded from plugin distribution.
 */
class CheckedClass {

  String name

  static constraints = {
    name(blank: false)
  }
}
