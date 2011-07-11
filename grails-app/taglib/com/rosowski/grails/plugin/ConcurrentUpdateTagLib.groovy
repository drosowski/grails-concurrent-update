package com.rosowski.grails.plugin

class ConcurrentUpdateTagLib {

  def grailsApplication
  def concurrentUpdateService

  static namespace = "conup"

  /**
   * Retrieves the value for a given field as it is stored in the database. Can be used to show cnocurrent changes
   * i.e. from another user to the editing user.
   *
   * @attrs bean REQUIRED the bean for which the field should be retrieved
   * @attrs field REQUIRED the name of the field which should be retrieved
   * @attrs optionKey OPTIONAL the optionKey for a select box (i.e. when selecting another domain class)
   * @attrs optionValue OPTIONAL the optionValue for a select box (i.e. when selecting another domain class)
   */
  def storedValue = { attrs, body ->
    def field = attrs.field
    def bean = attrs.bean
    def optionKey = attrs.optionKey
    def optionValue = attrs.optionValue

    if (field && bean) {
      def storedBean = concurrentUpdateService.getPersistentValues(bean)

      try {
        // fetch persistent value
        def value = storedBean."${field}_persisted"
        // render the stored value if it differs from the beans current value
        if (value && value != bean."${field}") {
          out << renderValue(value, field, optionKey, optionValue)
        }
      }
      catch (MissingPropertyException ex) {
        // ignore
      }
    }
  }

  private String renderValue(value, field, optionKey, optionValue) {
    StringBuilder builder = new StringBuilder()
    builder << "<div class='storedValue'>"

    if (value instanceof Date) {
      builder << g.formatDate(date: value, locale: request.locale)
    }
    else if (isGrailsDomainClass(value)) {
      builder << value."${optionValue}"
      builder << """<script type="text/javascript">
                  function getOptionForValue(optionValue) {
                    var selectBox = document.getElementById('${field}.${optionKey}');
                    if(selectBox) {
                      for(var i = 0; i < selectBox.options.length; i++) {
                        if(selectBox.options[i].value == optionValue) {
                          return selectBox.options[i];
                        }
                      }
                    }
                  }
                  </script>"""
      def keyValue = value."${optionKey}"
      builder << " <a href='#' onclick=\"var valueOption = getOptionForValue('${keyValue}'); if(valueOption) valueOption.selected = 'selected'\">"
      builder << "(" + g.message(code: "stored_value.transfer", default: "transfer") + ")"
      builder << "</a>"
    }
    else {
      builder << value.toString()
      builder << " <a href='#' onclick=\"document.getElementById('${field}').value = '${value.toString()}';\">"
      builder << "(" + g.message(code: "stored_value.transfer", default: "transfer") + ")"
      builder << "</a>"
    }

    builder << "</div>"
    return builder.toString()
  }

  boolean isGrailsDomainClass(aInstance) {
    return aInstance && grailsApplication.isDomainClass(aInstance.getClass())
  }

}
