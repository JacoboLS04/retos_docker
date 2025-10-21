package co.edu.uniquindio.retos.steps;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;

public class SmokeSteps {

    private int a;
    private int b;
    private int sum;

    @Given("two numbers {int} and {int}")
    public void two_numbers(int x, int y) {
        this.a = x;
        this.b = y;
    }

    @When("I add them")
    public void i_add_them() {
        this.sum = a + b;
    }

    @Then("the result should be {int}")
    public void the_result_should_be(int expected) {
        Assertions.assertEquals(expected, sum);
    }
}
