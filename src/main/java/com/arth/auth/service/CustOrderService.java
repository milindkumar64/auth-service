package com.arth.auth.service;

import com.arth.auth.model.Customer;
import com.arth.auth.model.Order;
import com.arth.auth.persist.CustomerRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@Service
public class CustOrderService {

    private static final Logger log = (Logger) LoggerFactory.getLogger(CustOrderService.class);

    @Autowired
    private  CustomerRepository customerRepository;

    @Transactional
    public void saveCustomerWithOrderDetails() {
        // Simulate saving order with customer details
        log.debug("Saving Customer with order details...");

        Customer customer = new Customer();
        customer.setName("John Doe");

        Order order1 = new Order();
        order1.setProduct("Laptop");
        order1.setQuantity(1);
        order1.setCustomer(customer); // Set the customer reference in the order

        Order order2 = new Order();
        order2.setProduct("Mouse");
        order2.setQuantity(2);
        order2.setCustomer(customer); // Set the customer reference in the order

        List<Order> orders = Arrays.asList(order1, order2);
        customer.setOrder(orders);

/*
        // 2nd way -- Add orders to the customer by calling addOrder method (this will set the customer reference in each order)
        customer.addOrder(order1);
        customer.addOrder(order2);
*/

        // Save the customer along with the orders (due to CascadeType.ALL, orders will be saved automatically)
        customerRepository.save(customer);

        // Simulate saving to the database
        log.info("Customer and Order saved successfully.");

    }

    public List<Order> checkOrder (String custName){

        List<Order> orders = customerRepository.findOrderbyCustomer(custName);
        log.info("Orders for customer {}: {}", custName, orders.toString());
        return orders;
    }
}
