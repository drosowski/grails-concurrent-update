package com.rosowski.grails.plugin

import org.hibernate.FlushMode
import org.hibernate.Session

class ConcurrentUpdateTagLib {

  static namespace = "conup"

  def storedValue = { attrs, body ->
    def field = attrs.field
    def bean = attrs.bean

    if (field && bean) {
      // possibly re-attach domain class
      bean.withSession { Session session ->
        try {
          session.setFlushMode(FlushMode.MANUAL)
          def persistentBean = session.get(bean.class.getName(), bean.id?.toLong())
          // only apply for concurrently updated domain classes
          if (persistentBean.version > bean.version) {
            // fetch persistent value
            def value = persistentBean."${field}"
            if (value) {
              out << "<div class='storedValue'>"
              out << g.message(code: "stored_value.message", args: [value, field])
              out << "</div>"
            }
          }
        } finally {
          session.setFlushMode(FlushMode.AUTO)
        }
      }
    }
  }
}
