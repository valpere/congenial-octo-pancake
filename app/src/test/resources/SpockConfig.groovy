runner {
    // Set a global timeout for all specifications
    // This prevents tests from running indefinitely
    timeout {
        // Default timeout in seconds for all specs
        seconds = 120
    }
}

report {
    // Include failed tests first in reports
    issueNamePrefix 'ISSUE-'
    issueUrlPrefix 'https://issues.example.org/'
}
