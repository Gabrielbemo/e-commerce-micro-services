package com.gabriel.ecommerce.customer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private CustomerController customerController;

    @Test
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest(null, "John", "Doe", "john@doe.com", null);
        when(customerService.createCustomer(request)).thenReturn("id-1");

        ResponseEntity<String> response = customerController.createCustomer(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("id-1");
    }

    @Test
    void shouldUpdateCustomer() {
        CustomerRequest request = new CustomerRequest("id-1", "John", "Doe", "john@doe.com", null);

        ResponseEntity<Void> response = customerController.updateCustomer(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        verify(customerService).updateCustomer(request);
    }

    @Test
    void shouldReturnAllCustomers() {
        List<CustomerResponse> customers = List.of(new CustomerResponse("id-1", "John", "Doe", "john@doe.com", null));
        when(customerService.findAllCustomers()).thenReturn(customers);

        ResponseEntity<List<CustomerResponse>> response = customerController.getAllCustomers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactlyElementsOf(customers);
    }

    @Test
    void shouldCheckCustomerExistence() {
        when(customerService.existsById("id-1")).thenReturn(true);

        ResponseEntity<Boolean> response = customerController.customerExists("id-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    void shouldFindCustomerById() {
        CustomerResponse customer = new CustomerResponse("id-1", "John", "Doe", "john@doe.com", null);
        when(customerService.findById("id-1")).thenReturn(customer);

        ResponseEntity<CustomerResponse> response = customerController.findById("id-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(customer);
    }

    @Test
    void shouldDeleteCustomer() {
        ResponseEntity<Void> response = customerController.deleteCustomer("id-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(customerService).deleteCustomer("id-1");
    }
}
