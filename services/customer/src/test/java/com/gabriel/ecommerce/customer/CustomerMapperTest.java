package com.gabriel.ecommerce.customer;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerMapperTest {

    private final CustomerMapper customerMapper = new CustomerMapper();

    @Test
    void shouldReturnNullWhenRequestIsNull() {
        assertThat(customerMapper.toCustomer(null)).isNull();
    }

    @Test
    void shouldMapRequestToCustomer() {
        Address address = Address.builder().street("Main St").houseNumber("100").zipCode("12345").build();
        CustomerRequest request = new CustomerRequest("id-1", "John", "Doe", "john@doe.com", address);

        Customer customer = customerMapper.toCustomer(request);

        assertThat(customer.getId()).isNull();
        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Doe");
        assertThat(customer.getEmail()).isEqualTo("john@doe.com");
        assertThat(customer.getAddress()).isEqualTo(address);
    }

    @Test
    void shouldMergeOnlyNotBlankValues() {
        Address originalAddress = Address.builder().street("Old").houseNumber("1").zipCode("00000").build();
        Customer customer = Customer.builder()
                .id("id-1")
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .address(originalAddress)
                .build();

        Address newAddress = Address.builder().street("New").houseNumber("2").zipCode("99999").build();
        CustomerRequest request = new CustomerRequest("id-1", "", "Smith", " ", newAddress);

        customerMapper.mergeCustomer(customer, request);

        assertThat(customer.getFirstName()).isEqualTo("John");
        assertThat(customer.getLastName()).isEqualTo("Smith");
        assertThat(customer.getEmail()).isEqualTo("john@doe.com");
        assertThat(customer.getAddress()).isEqualTo(newAddress);
    }

    @Test
    void shouldMapCustomerToResponse() {
        Address address = Address.builder().street("Main St").houseNumber("100").zipCode("12345").build();
        Customer customer = Customer.builder()
                .id("id-1")
                .firstName("John")
                .lastName("Doe")
                .email("john@doe.com")
                .address(address)
                .build();

        CustomerResponse response = customerMapper.toCustomerResponse(customer);

        assertThat(response.id()).isEqualTo("id-1");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.email()).isEqualTo("john@doe.com");
        assertThat(response.address()).isEqualTo(address);
    }
}
