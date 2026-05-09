package com.arth.auth;

import com.arth.auth.model.Order;
import com.arth.auth.service.CustOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/customer")
public class CutomerOrderController {

    @Autowired
    private CustOrderService custOrderService;

    @PostMapping("/cust-order")
    public String placeOrder(@RequestParam String custName) {
        // In a real application, you would process the order details and save it to the database
        custOrderService.saveCustomerWithOrderDetails(custName);
        return "Order placed successfully: ";
    }

    @GetMapping("/check-order")
    public List<Order> checkOrder(@RequestParam String custName) {
        // In a real application, you would process the order details and save it to the database
        return custOrderService.checkOrder(custName);
    }

}
