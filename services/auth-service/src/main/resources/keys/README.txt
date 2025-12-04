IMPORTANT: RSA Key Files for Local Development Only

These RSA key files (private.pem and public.pem) are for LOCAL DEVELOPMENT ONLY.

SECURITY WARNINGS:
==================
1. DO NOT commit private.pem to version control in production
2. DO NOT use these keys in production environments
3. Dev-only keys may be present in classpath for local testing. DO NOT store private keys in git for production. Use Vault or K8s secrets. If private.pem exists in repo, rotate immediately.
4. In production, store keys in:
   - HashiCorp Vault
   - Kubernetes Secrets
   - AWS Secrets Manager / Azure Key Vault
   - Environment variables (for public key only)
   - Secure file mounts (for private key)

PRODUCTION SETUP:
=================
- Set jwt.private-key-file and jwt.public-key-file in application-prod.yml to:
  - file:/path/to/mounted/secret/private.pem
  - Or use environment variables: ${JWT_PRIVATE_KEY} and ${JWT_PUBLIC_KEY}
  - Or load from secrets manager via Spring Cloud Config / Vault

GENERATING NEW KEYS:
====================
If you need to regenerate these keys for local development:

  openssl genrsa -out private.pem 2048
  openssl rsa -in private.pem -pubout -out public.pem

These keys are 2048-bit RSA keys suitable for RS256 JWT signing.

