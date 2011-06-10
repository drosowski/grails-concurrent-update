package com.rosowski.grails.plugin

import org.codehaus.groovy.grails.web.binding.BindEventListener
import org.springframework.beans.MutablePropertyValues
import org.springframework.beans.TypeConverter

/**
 * Includes the version property in the autobinding process.
 */
class IncludeVersionListener implements BindEventListener {

  void doBind(Object target, MutablePropertyValues source, TypeConverter typeConverter) {
    def version = source.getPropertyValue("version")
    if (version != null) {
      target.version = version.getValue()?.toLong()
    }
  }

}
