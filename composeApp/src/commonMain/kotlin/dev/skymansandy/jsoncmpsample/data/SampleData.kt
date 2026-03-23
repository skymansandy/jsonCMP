package dev.skymansandy.jsoncmpsample.data

val sampleJson = """
{
    "name": "John Doe",
    "age": 30,
    "isActive": true,
    "address": {
        "street": "123 Main St",
        "city": "New York",
        "zip": {
            "name": "John Doe",
            "age": 30,
            "isActive": true,
            "address": {
                "street": "123 Main St",
                "city": "New York",
                "zip": "10001"
            },
            "tags": ["developer", "kotlin", "compose"],
            "score": {
                "name": "John Doe",
                "age": 30,
                "isActive": true,
                "address": {
                    "street": "123 Main St",
                    "city": "New York",
                    "zip": "10001"
                },
                "tags": ["developer", "kotlin", "compose"],
                "score": {
    "name": "John Doe",
    "age": 30,
    "isActive": true,
    "address": {
        "street": "123 Main St",
        "city": "New York",
        "zip": {
            "name": "John Doe",
            "age": 30,
            "isActive": true,
            "address": {
                "street": "123 Main St",
                "city": "New York",
                "zip": "10001"
            },
            "tags": ["developer", "kotlin", "compose"],
            "score": {
                "name": "John Doe",
                "age": 30,
                "isActive": true,
                "address": {
                    "street": "123 Main St",
                    "city": "New York",
                    "zip": "10001"
                },
                "tags": ["developer", "kotlin", "compose"],
                "score": null
            }
        }
    },
    "tags": ["developer", "kotlin", "compose"],
    "score": null
}
            }
        }
    },
    "tags": ["developer", "kotlin", "compose"],
    "score": null
}
""".trimIndent()
