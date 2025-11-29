package hello.itemservice.web.validation;

import hello.itemservice.domain.item2.ItemSaveForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/validation/api/items")
public class ValidationItemApiController {
    @PostMapping("/add")
    public Object addItem(
            @RequestBody @Validated ItemSaveForm form,
            BindingResult bindingResult
    ) {
        if(bindingResult.hasErrors()) {
            return bindingResult.getAllErrors();
        }
        return form;
        // postman으로 확인 (Body-raw-JSON)
        /*
        POST http://localhost:8080/validation/api/items/add
        {"itemName":"1", "price":1, "quantity": 1}
         */
        /*
        [
            {
                "codes": [
                    "Range.itemSaveForm.price",
                    "Range.price",
                    "Range.java.lang.Integer",
                    "Range"
                ],
                "arguments": [
                    {
                        "codes": [
                            "itemSaveForm.price",
                            "price"
                        ],
                        "arguments": null,
                        "defaultMessage": "price",
                        "code": "price"
                    },
                    200,
                    100
                ],
                "defaultMessage": "100에서 200 사이여야 합니다",
                "objectName": "itemSaveForm",
                "field": "price",
                "rejectedValue": 1,
                "bindingFailure": false,
                "code": "Range"
            },
            {
                "codes": [
                    "Range.itemSaveForm.quantity",
                    "Range.quantity",
                    "Range.java.lang.Integer",
                    "Range"
                ],
                "arguments": [
                    {
                        "codes": [
                            "itemSaveForm.quantity",
                            "quantity"
                        ],
                        "arguments": null,
                        "defaultMessage": "quantity",
                        "code": "quantity"
                    },
                    100,
                    10
                ],
                "defaultMessage": "10에서 100 사이여야 합니다",
                "objectName": "itemSaveForm",
                "field": "quantity",
                "rejectedValue": 1,
                "bindingFailure": false,
                "code": "Range"
            }
        ]
         */
    }
}
