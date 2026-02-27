package com.arth.auth.persist;

import com.arth.auth.model.Customer;
import com.arth.auth.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional <Customer> findById(Long id);

    @Query("SELECT c FROM Customer c JOIN c.order o where o.id = :id")
    Customer findCustomerbyOrder(Long id); // only customer details will be fetched, order details will not be fetched
    // customer will be filtered based on order id, but order details will not be fetched
    @Query("SELECT o FROM Order o JOIN o.customer c where c.name = :name")
    List<Order> findOrderbyCustomer(String name); // only order details will be fetched, customer details will not be fetched
    // order will be filtered based on customer id, but customer details will not be fetched
    @Query("SELECT c FROM Customer c JOIN FETCH c.order o where o.id = :id")
    Customer findCustomerWithOrderDetails(Long id); // both customer and order details will be fetched



}
