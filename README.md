# Smart Task Manager

A Swing-based task manager with weather-aware scheduling, built with Project Reactor.

## Prerequisites

* Java 17+
* Maven
* Internet connection (for weather API)

## Configuration

The app fetches live weather data from [OpenWeatherMap](https://openweathermap.org/api).
You must set your API key as an environment variable before running:

### Mac / Linux

```bash
export OPENWEATHER_API_KEY=your_api_key_here
```

### Windows

```cmd
set OPENWEATHER_API_KEY=your_api_key_here
```

If the API key is missing or invalid, weather data will be unavailable, but the application will still run.

## Running

```bash
mvn compile exec:java -Dexec.mainClass="taskmanager.MainApp"
```

## Features

* Create, edit, delete, and view tasks
* Mark tasks as indoor or outdoor
* Fetch real-time weather data
* Provide weather-based recommendations
* Responsive Swing GUI
* Reactive programming using Project Reactor (`Mono`)

## Project Structure

```
taskmanager/
├── api/         # Interfaces (TaskManager, TaskService, SchedulePlanner)
├── model/       # Data classes (Task, WeatherForecast, ScheduleRecommendation)
├── service/     # Weather API integration
├── impl/        # Implementations
├── exception/   # Custom exceptions
├── ui/swing/    # Swing GUI
```

## Example Usage

```java
import taskmanager.api.TaskManager;
import taskmanager.impl.TaskManagerImpl;
import taskmanager.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;

public class Example {
    public static void main(String[] args) {
        TaskManager manager = new TaskManagerImpl();

        Task task = new Task(
            UUID.randomUUID().toString(),
            "Go for a walk",
            LocalDateTime.now(),
            true // outdoor task
        );

        manager.createTask(task)
               .flatMap(manager::getRecommendation)
               .subscribe(rec ->
                   System.out.println(rec.getMessage())
               );
    }
}
```

## Notes

* The application uses environment variables for API key security.
* Do not hardcode API keys in the source code.
* Weather data is cached briefly to reduce API calls.

## Author

* Ghaida