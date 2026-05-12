import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "${entityName} API contract"

    request {
        method GET()
        url "/api/v1/${moduleName}"
        headers {
            contentType applicationJson()
        }
    }

    response {
        status OK()
        headers {
            contentType applicationJson()
        }
        body([
            [
                id: $(anyUuid()),
                name: $(anyNonBlankString())
            ]
        ])
    }
}
