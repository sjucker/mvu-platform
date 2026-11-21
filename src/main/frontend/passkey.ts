// Client side of the WebAuthn ceremonies handled by Spring Security's webauthn filters.
// The CSRF header and token are handed in from the server side Vaadin view.

function encode(buffer: ArrayBuffer): string {
  const base64 = window.btoa(String.fromCharCode(...new Uint8Array(buffer)));
  return base64.replace(/=/g, '').replace(/\+/g, '-').replace(/\//g, '_');
}

function decode(base64url: string): ArrayBuffer {
  const binary = window.atob(base64url.replace(/-/g, '+').replace(/_/g, '/'));
  const bytes = new Uint8Array(binary.length);
  for (let i = 0; i < binary.length; i++) {
    bytes[i] = binary.charCodeAt(i);
  }
  return bytes.buffer;
}

async function post(url: string, csrfHeader: string, csrfToken: string, body?: unknown): Promise<any> {
  const response = await fetch(url, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', [csrfHeader]: csrfToken },
    body: body ? JSON.stringify(body) : undefined,
  });
  if (!response.ok) {
    throw new Error(`${url} responded with HTTP ${response.status}`);
  }
  return response.json();
}

async function login(csrfHeader: string, csrfToken: string): Promise<void> {
  const options = await post('webauthn/authenticate/options', csrfHeader, csrfToken);
  const credential = (await navigator.credentials.get({
    publicKey: {
      ...options,
      challenge: decode(options.challenge),
      allowCredentials: (options.allowCredentials ?? []).map((c: any) => ({ ...c, id: decode(c.id) })),
    },
  })) as PublicKeyCredential;
  const response = credential.response as AuthenticatorAssertionResponse;

  const result = await post('login/webauthn', csrfHeader, csrfToken, {
    id: credential.id,
    rawId: encode(credential.rawId),
    response: {
      authenticatorData: encode(response.authenticatorData),
      clientDataJSON: encode(response.clientDataJSON),
      signature: encode(response.signature),
      userHandle: response.userHandle ? encode(response.userHandle) : undefined,
    },
    credType: credential.type,
    clientExtensionResults: credential.getClientExtensionResults(),
    authenticatorAttachment: credential.authenticatorAttachment,
  });
  if (!result?.authenticated) {
    throw new Error('Passkey authentication failed');
  }
  window.location.href = result.redirectUrl;
}

async function register(csrfHeader: string, csrfToken: string, label: string): Promise<void> {
  const options = await post('webauthn/register/options', csrfHeader, csrfToken);
  const credential = (await navigator.credentials.create({
    publicKey: {
      ...options,
      user: { ...options.user, id: decode(options.user.id) },
      challenge: decode(options.challenge),
      excludeCredentials: (options.excludeCredentials ?? []).map((c: any) => ({ ...c, id: decode(c.id) })),
    },
  })) as PublicKeyCredential;
  const response = credential.response as AuthenticatorAttestationResponse;

  const result = await post('webauthn/register', csrfHeader, csrfToken, {
    publicKey: {
      credential: {
        id: credential.id,
        rawId: encode(credential.rawId),
        response: {
          attestationObject: encode(response.attestationObject),
          clientDataJSON: encode(response.clientDataJSON),
          transports: response.getTransports ? response.getTransports() : [],
        },
        type: credential.type,
        clientExtensionResults: credential.getClientExtensionResults(),
        authenticatorAttachment: credential.authenticatorAttachment,
      },
      label,
    },
  });
  if (!result?.success) {
    throw new Error('Passkey registration failed');
  }
}

function isSupported(): boolean {
  return !!window.PublicKeyCredential;
}

(window as any).mvuPasskey = { login, register, isSupported };
