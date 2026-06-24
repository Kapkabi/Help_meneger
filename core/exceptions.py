class AppError(Exception):
    """Base class for all application-level errors shown to the user."""


class ValidationError(AppError):
    """Raised when user-supplied recipe data fails business validation."""


class RepositoryError(AppError):
    """Raised when a data-access operation fails (wraps sqlite3 errors)."""


class DatabaseConnectionError(AppError):
    """Raised when the SQLite database file cannot be opened."""


class DatabaseMigrationError(AppError):
    """Raised when applying a schema migration fails (e.g. corrupted DB)."""
