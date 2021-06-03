### v1.0.2
- Feature
  - Stack trace duplicates logged by hash reference
  - variables now have 2 separate properties
    - `name` refers to the text to use when logging
    - `expression` the variable reference or expression to use to extract the value
  - logWhen condition for variables now uses 'name' instead of 'index' to refer to the variable to apply the logging condition.
  - count total executions for each tracer

### v0.5.1
- Initial release for general use