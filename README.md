# Nuraly Advanced ACL System

An enterprise-grade Access Control List (ACL) system built with Quarkus, inspired by PowerApps security model with full support for anonymous access, delegation, and multi-tenant architecture.

## 🚀 Features

### Core ACL Capabilities
- **Fine-grained permissions**: Custom permissions beyond CRUD (read, write, annotate, publish, share, moderate, etc.)
- **Multi-scope permissions**: Application, Organization, and Resource level permissions
- **Role-based access control**: Hierarchical roles with inheritance
- **Anonymous/Public access**: Public sharing with configurable permissions and expiration
- **Delegation system**: Users can share resources with others using predefined roles
- **Real-time revocation**: Immediate permission changes with clean propagation
- **Multi-tenancy**: Complete tenant isolation with shared system roles

### PowerApps-Inspired Features
- **Security roles**: Reusable permission bundles (Viewer, Editor, Publisher, Moderator)
- **Custom permission sets**: Define organization-specific permissions
- **Record-level access control**: Per-resource permission management
- **Sharing policies**: Templates like "anyone with the link can view"
- **Environment-level security**: Tenant-wide permission management
- **Ownership-based security**: Automatic owner permissions

### Advanced Security
- **Audit logging**: Complete audit trail of all permission changes
- **Public link generation**: Secure token-based anonymous access
- **Time-limited access**: Permissions with expiration dates
- **Usage tracking**: Monitor resource access patterns

This project uses Quarkus, the Supersonic Subatomic Java Framework. If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/nuraly-permissions-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)
