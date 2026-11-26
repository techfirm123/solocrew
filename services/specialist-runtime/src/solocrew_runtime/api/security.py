"""Authentication for trusted internal SoloCrew service calls."""

import os
import secrets
from typing import Annotated

from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPAuthorizationCredentials, HTTPBearer

_bearer_scheme = HTTPBearer(auto_error=False)


def internal_token_is_configured() -> bool:
    """Return whether the internal service token is configured."""

    return bool(os.getenv("SOLOCREW_INTERNAL_TOKEN", "").strip())


def require_internal_worker_access(
    credentials: Annotated[
        HTTPAuthorizationCredentials | None,
        Depends(_bearer_scheme),
    ],
) -> None:
    """Require a valid shared bearer token for internal execution."""

    expected_token = os.getenv("SOLOCREW_INTERNAL_TOKEN", "").strip()

    if not expected_token:
        raise HTTPException(
            status_code=status.HTTP_503_SERVICE_UNAVAILABLE,
            detail="Internal execution authentication is not configured.",
        )

    if credentials is None:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="A valid internal bearer token is required.",
            headers={"WWW-Authenticate": "Bearer"},
        )

    if not secrets.compare_digest(credentials.credentials, expected_token):
        raise HTTPException(
            status_code=status.HTTP_403_FORBIDDEN,
            detail="The internal bearer token is invalid.",
        )
