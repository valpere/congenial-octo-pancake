
import java.util.concurrent.TimeUnit
                
runner {
    // Global timeout for all tests to prevent hanging tests
    filterStackTrace = true
    optimizeRunOrder = true
}
                
// Configure default timeout for all feature methods
spock {
    timeout {
        enabled = true
        unit = TimeUnit.SECONDS
        value = 60
    }
}
