import LunchManCore.Storage;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import play.db.Database;
import play.db.Databases;
import services.CSVRepository;
import services.PostgresRepository;

public class Module extends AbstractModule{
    @Override
    protected void configure() {
        bind(Storage.class).to(PostgresRepository.class);
    }

    @Provides
    PostgresRepository providePostgresRepository() {
        Database db = Databases.createFrom("org.postgresql.Driver", "jdbc:postgresql://localhost/lunchman_development");
        return new PostgresRepository(db);
    }

    @Provides
    CSVRepository provideCSVRepository() {
        CSVRepository csv = new CSVRepository(
                "apprentices.csv", "restaurants.csv", "schedule.csv", "employees.csv", "guests.csv");
        return csv;
    }
}
