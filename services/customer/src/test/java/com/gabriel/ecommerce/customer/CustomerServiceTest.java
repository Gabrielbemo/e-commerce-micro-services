package com.gabriel.ecommerce.customer;

import com.gabriel.ecommerce.exception.CustomerNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;

    @InjectMocks
    private CustomerService customerService;

    @Test
    void shouldCreateCustomer() {
        CustomerRequest request = new CustomerRequest(null, "John", "Doe", "john@doe.com", null);
        Customer mapped = Customer.builder().firstName("John").build();
        Customer saved = Customer.builder().id("id-1").build();

        when(customerMapper.toCustomer(request)).thenReturn(mapped);
        when(customerRepository.save(mapped)).thenReturn(saved);

        String id = customerService.createCustomer(request);

        assertThat(id).isEqualTo("id-1");
        verify(customerMapper).toCustomer(request);
        verify(customerRepository).save(mapped);
    }

    @Test
    void shouldUpdateCustomer() {
        CustomerRequest request = new CustomerRequest("id-1", "John", "Doe", "john@doe.com", null);
        Customer customer = Customer.builder().id("id-1").build();

        when(customerRepository.findById("id-1")).thenReturn(Optional.of(customer));

        customerService.updateCustomer(request);

        verify(customerMapper).mergeCustomer(customer, request);
        verify(customerRepository).save(customer);
    }

    @Test
    void shouldThrowWhenUpdatingUnknownCustomer() {
        CustomerRequest request = new CustomerRequest("id-1", "John", "Doe", "john@doe.com", null);
        when(customerRepository.findById("id-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateCustomer(request))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("id-1");

        verify(customerMapper, never()).mergeCustomer(any(), any());
        verify(customerRepository, never()).save(any());
    }

    @Test
    void shouldFindAllCustomers() {
        Customer john = Customer.builder().id("id-1").build();
        Customer jane = Customer.builder().id("id-2").build();
        CustomerResponse johnResponse = new CustomerResponse("id-1", "John", "Doe", "john@doe.com", null);
        CustomerResponse janeResponse = new CustomerResponse("id-2", "Jane", "Doe", "jane@doe.com", null);

        when(customerRepository.findAll()).thenReturn(List.of(john, jane));
        when(customerMapper.toCustomerResponse(john)).thenReturn(johnResponse);
        when(customerMapper.toCustomerResponse(jane)).thenReturn(janeResponse);

        List<CustomerResponse> responses = customerService.findAllCustomers();

        assertThat(responses).containsExactly(johnResponse, janeResponse);
    }

    @Test
    void shouldValidateIfCustomerExistsById() {
        when(customerRepository.existsById("id-1")).thenReturn(true);

        assertThat(customerService.existsById("id-1")).isTrue();
        assertThat(customerService.existsById(" ")).isFalse();
        assertThat(customerService.existsById(null)).isFalse();
    }

    @Test
    void shouldFindCustomerById() {
        Customer customer = Customer.builder().id("id-1").build();
        CustomerResponse response = new CustomerResponse("id-1", "John", "Doe", "john@doe.com", null);

        when(customerRepository.findById("id-1")).thenReturn(Optional.of(customer));
        when(customerMapper.toCustomerResponse(customer)).thenReturn(response);

        CustomerResponse result = customerService.findById("id-1");

        assertThat(result).isEqualTo(response);
    }

    @Test
    void shouldThrowWhenFindingUnknownCustomerById() {
        when(customerRepository.findById("id-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById("id-1"))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("id-1");
    }

    @Test
    void shouldDeleteCustomerById() {
        customerService.deleteCustomer("id-1");

        verify(customerRepository).deleteById("id-1");
    }
}
