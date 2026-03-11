const PROXY_CONFIG = [
    {
        context: [
            "/backend",
        ],
        target: "http://localhost",
        secure: false
    }
]

module.exports = PROXY_CONFIG;