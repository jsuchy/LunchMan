import LunchManCore.Employee;
import LunchManCore.FridayLunch;
import LunchManCore.Guest;
import LunchManCore.Storage;
import controllers.HomeController;
import controllers.routes;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import play.*;
import play.inject.guice.GuiceApplicationBuilder;
import play.mvc.Result;
import play.test.Helpers;
import play.test.WithApplication;
import services.CSVRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static play.test.Helpers.*;


public class HomeControllerTest extends WithApplication{

    private List<FridayLunch> loadedSchedule;
    private List<Employee> loadedEmployees;
    private List<Guest> loadedGuests;
    private HomeController homeController;
    private Storage storage;

    @Override
    protected Application provideApplication() {
        return new GuiceApplicationBuilder()
                .configure("play.http.router", "router.Routes")
                .build();
    }

    @Before
    public void setUp() throws IOException {
        storage = new CSVRepository(Environment.simple(), "test/resources/apprentices.csv", "test/resources/restaurants.csv", "test/resources/schedule.csv", "test/resources/employees.csv", "test/resources/guests.csv");
        loadedSchedule = storage.getSchedule();
        loadedEmployees = storage.getEmployees();
        loadedGuests = storage.getGuests();
        homeController = new HomeController(storage);
    }

    @After
    public void tearDown() throws Exception {
        storage.saveSchedule(loadedSchedule);
        storage.saveEmployees(loadedEmployees);
        storage.saveGuests(loadedGuests);
    }

    @Test
    public void indexPage() {
        Result result = route(fakeRequest("GET", "/"));
        assertEquals(OK, result.status());
    }

    @Test
    public void cssAssets() {
        Result result = route(fakeRequest("GET", "/assets/stylesheets/main.css"));
        assertEquals(OK, result.status());
    }

    @Test
    public void indexPageShowsApprenticeNames() {
        Result result = route(routes.HomeController.index());
        assertTrue(contentAsString(result).contains("LunchMan"));
        assertTrue(contentAsString(result).contains("Priya"));
        assertTrue(contentAsString(result).contains("Mollie"));
        assertTrue(contentAsString(result).contains("Nick"));
        assertTrue(contentAsString(result).contains("Rabea"));
    }

    @Test
    public void changeApprenticeRedirectsToIndex() throws Exception {
        Map form = new HashMap<String, String>();
        form.put("position", "0");
        form.put("newName", "Ced");

        Result postResult = route(fakeRequest(routes.HomeController.changeSchedule()).bodyForm(form));

        assertEquals(SEE_OTHER, postResult.status());
        assertEquals("/", postResult.header("Location").get());
    }

    @Test
    public void canChangeShownApprenticeForADay() throws Exception {
        Map form = new HashMap<String, String>();
        form.put("position", "0");
        form.put("newName", "Ced");

        invokeWithContext(Helpers.fakeRequest().bodyForm(form), () -> homeController.changeSchedule());

        Result result = homeController.index();
        assertTrue(contentAsString(result).contains("Ced"));
    }

    @Test
    public void asksForMenuChoice() throws Exception {
        Result result = homeController.index();
        assertTrue(contentAsString(result).contains("Please choose a menu:"));
    }

    @Test
    public void assignMenuRedirectsToIndex() throws Exception {
        Map form = new HashMap<String, String>();
        form.put("restaurant", "2");

        Result postResult = route(fakeRequest(routes.HomeController.assignMenu()).bodyForm(form));

        assertEquals(SEE_OTHER, postResult.status());
        assertEquals("/", postResult.header("Location").get());
    }

    @Test
    public void canAssignAMenuToAFridayLunch() throws Exception {
        Map form = new HashMap<String, String>();
        form.put("restaurant", "2");

        invokeWithContext(Helpers.fakeRequest().bodyForm(form),
                () -> homeController.assignMenu());

        Result result = homeController.index();
        assertTrue(contentAsString(result).contains("Deliveroo"));
    }

    @Test
    public void asksForOrderChoice() throws Exception {
        chooseMenu();

        Result result = homeController.index();

        assertTrue(contentAsString(result).contains("Please add your order"));
    }

    @Test
    public void addingOrderRedirectsToIndex() throws Exception {
        chooseMenu();
        Map form = new HashMap<String, String>();
        form.put("name", "1");
        form.put("order", "Peri Peri Chicken");

        Result orderResult = route(fakeRequest(routes.HomeController.newOrder()).bodyForm(form));

        assertEquals(SEE_OTHER, orderResult.status());
        assertEquals("/", orderResult.header("Location").get());
    }


    @Test
    public void canAddAnOrderToAFridayLunch() throws Exception {
        chooseMenu();
        Map form = new HashMap<String, String>();
        form.put("name", "1");
        form.put("order", "Peri Peri Chicken");

        invokeWithContext(Helpers.fakeRequest().bodyForm(form),
                () -> homeController.newOrder());

        Result result = homeController.index();
        assertTrue(contentAsString(result).contains("Peri Peri Chicken"));
    }

    @Test
    public void removeOrderRedirectsToIndex() throws Exception {
        chooseMenu();
        placeOrder("1", "Peri Peri Chicken");
        Map form = new HashMap<String, String>();
        form.put("name", "1");

        Result result = route(fakeRequest(routes.HomeController.deleteOrder()).bodyForm(form));

        assertEquals(SEE_OTHER, result.status());
        assertEquals("/", result.header("Location").get());
    }

    @Test
    public void canRemoveAnOrder() throws Exception {
        chooseMenu();
        placeOrder("1", "Peri Peri Chicken");
        Map form = new HashMap<String, String>();
        form.put("name", "1");

        invokeWithContext(Helpers.fakeRequest().bodyForm(form),
                () -> homeController.deleteOrder());

        Result result = homeController.index();
        assertFalse(contentAsString(result).contains("Peri Peri Chicken"));

    }

    @Test
    public void addingGuestRedirectsToIndex() throws Exception {
        chooseMenu();
        Map form = new HashMap<String, String>();
        form.put("name", "Gary");
        form.put("order", "Peri Peri Chicken");

        Result orderResult = route(fakeRequest(routes.HomeController.newGuest()).bodyForm(form));

        assertEquals(SEE_OTHER, orderResult.status());
        assertEquals("/", orderResult.header("Location").get());
    }

    @Test
    public void canAddAGuest() throws Exception {
        chooseMenu();
        Map form = new HashMap<String, String>();
        form.put("name", "Gary");
        form.put("order", "Tuna Melt");

        invokeWithContext(Helpers.fakeRequest().bodyForm(form),
                () -> homeController.newGuest());

        Result result = homeController.index();
        assertTrue(contentAsString(result).contains("Gary"));
        assertTrue(contentAsString(result).contains("Tuna Melt"));
    }

    private void placeOrder(String position, String order) {
        Map orderForm = new HashMap<String, String>();
        orderForm.put("name", position);
        orderForm.put("order", order);
        invokeWithContext(Helpers.fakeRequest().bodyForm(orderForm),
                () -> homeController.newOrder());
    }

    private void chooseMenu() {
        Map restaurantForm = new HashMap<String, String>();
        restaurantForm.put("restaurant", "2");
        invokeWithContext(Helpers.fakeRequest().bodyForm(restaurantForm),
                () -> homeController.assignMenu());
    }
}
