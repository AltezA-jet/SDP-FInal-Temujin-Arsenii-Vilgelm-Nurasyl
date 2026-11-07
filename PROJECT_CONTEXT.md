1. Your Task
Choose a problem that interests your team and develop an application that addresses it. You may choose any domain: education, games, travel, sports, music, ecology, university life, event management, or your own idea.
Your application must support a complete user journey:
Create or select something >>>> configure it >>>> perform an action >>>> view and save the result.
For example, a user creates a travel plan, compares route options, calculates the cost, and saves the itinerary.
The main goal is to demonstrate how design patterns help you structure an application and extend its functionality.
2. Project Ideas
You may choose, adapt, or replace any idea below. The requirements and assessment criteria are the same for all topics.
Project idea	Possible features
Campus Quest	Create university quests with different task types, routes, hints, and participant progress
Trip With Friends	Plan group trips with budgets, activities, route selection strategies, and change notifications
Hackathon Control Room	Manage teams, challenges, assessment criteria, results, and announcements
Build Your Setup	Configure a gaming or work setup with compatibility checks, optional upgrades, and price calculation
Study Sprint	Build study plans with different scheduling strategies and progress tracking
Event Studio	Create events with service packages, optional extras, bookings, and status updates
Eco Campus	Manage environmental initiatives, resource collection, impact calculations, and notifications
Game Lab	Develop a small game or simulation with character creation, interchangeable behaviors, upgrades, and events
Creator Studio	Create publications or documents with different content types, formatting options, previews, and export
Pet Care Planner	Plan pet care with procedures, schedules, additional services, and in-app reminders
3. Common Functional Requirements
Every project must include:
1.	At least three related domain entities, such as Event, Participant, and Booking.
2.	At least five user scenarios, such as creating an event, configuring a package, making a booking, changing its status, and viewing a summary.
3.	Creation, viewing, and editing of core data. Include deletion or cancellation where appropriate.
4.	Interchangeable behavior: users must be able to choose between at least two ways of performing an operation, such as different calculation, selection, or processing algorithms.
5.	Event-driven behavior: a data change must trigger a meaningful reaction, such as an interface update or notification.
6.	Persistent storage: data must remain available after restarting the application. Files, JSON, or a database are acceptable.
7.	A final output: a report, summary, plan, order, game result, or another meaningful result that can be saved or exported.
8.	Input validation and error handling: handle empty fields, invalid values, and actions that violate application rules.
Provide sample data so that the application can be demonstrated immediately.
4. Design Pattern Requirements
Implement at least six distinct patterns covered in the course, including at least one creational, one structural, and one behavioral pattern.
Category	Available patterns
Creational	Builder, Factory Method, Abstract Factory
Structural	Adapter, Bridge, Decorator, Facade
Behavioral	Strategy, Observer, Visitor
Your team chooses the six patterns. Each choice must address a real design problem in your application.
Each student must independently implement at least two patterns. A two-person team still needs six distinct patterns—for example, three per student.
A pattern counts only when:
•	It is implemented in your code and used by the working application.
•	Your team demonstrates a scenario in which it performs its intended role.
•	The responsible student explains its participants, interactions, and benefits.
Naming a class Factory or Facade does not prove that the pattern is correctly implemented. Patterns implemented internally by a framework do not count as your own implementations.
Include this table in your report:
Pattern	Problem it solves	Main classes/interfaces	How to demonstrate it	Responsible student
All six patterns must form part of one integrated application.
5. User Interface Requirements
A web interface or desktop GUI is required.
Users must be able to:
•	Browse the main objects.
•	Create and edit data.
•	Configure available options and behaviors.
•	Complete the main user journey.
•	View results, validation messages, and errors.
You may use separate pages, tabs, or panels within one window. The number of screens is not an assessment criterion.
Implement the core application logic and design patterns in Java or Go. For a web interface, you may use HTML, CSS, JavaScript, and suitable libraries.
The interface must connect to the actual application logic: user actions must change data and produce real results.
6. Code Quality and Testing
Separate the interface, business logic, and data storage. Use meaningful names, focused methods, and avoid duplicated code.
Prepare at least eight test scenarios, covering:
•	The main successful user journey.
•	Switching between alternative behaviors.
•	A reaction to an event.
•	Saving and reloading data.
•	At least three invalid-input or boundary cases.
For each scenario, record the inputs, expected result, and actual result. Automate at least three tests of business logic.
7. Three-Week Development Plan
Stage	Expected outcome
Week 1 — Idea and architecture	Problem and target users, five user scenarios, interface sketch, six selected patterns, task allocation, initial UML, and a runnable application skeleton
Week 2 — Working version	Main user journey works through the interface; all six patterns are integrated; data persistence works
Week 3 — Completion	Error handling, tests, bug fixes, sample data, final UML, report, and defense preparation
Present your progress to the instructor at the end of each week.
According to the syllabus, final materials must be submitted by the end of course Week 9, with the defense taking place in Week 10. The three-week development period must finish before the submission deadline.
8. Submission Requirements
Submit the following through Moodle:
•	Source code in a ZIP archive, plus a GitHub/GitLab repository link showing the team’s development history.
•	README: environment requirements, setup and launch commands, sample data, and instructions for checking the application.
•	PDF report containing:
o	Introduction.
o	Main Body: user scenarios, architecture, pattern table, UML diagrams, screenshots, test results, and individual contributions.
o	Conclusion.
o	Future Work.
•	Include relevant Java/Go code excerpts explaining your pattern implementations in the report.
Required diagrams:
•	A class diagram showing the pattern participants and their relationships.
•	A sequence diagram showing one main application scenario.
9. Project Defense
The team demonstrates the application and completes its main user journey.
Each student must then individually:
1.	Show their patterns in the code and working application.
2.	Explain why they selected those patterns and discuss their limitations.
3.	Answer questions about architecture and component interactions.
4.	Make a small change requested by the instructor - for example, add a strategy, product variant, decorator, or subscriber relevant to their contribution.
