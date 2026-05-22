import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";

// 1. Update path agar sesuai dengan struktur folder app/auth/...
const PUBLIC_PATHS = ["/auth/login", "/auth/register", "/"];

export function proxy(request: NextRequest) {
    const { pathname } = request.nextUrl;

    const isPublic = PUBLIC_PATHS.some(
        (p) => pathname === p || pathname.startsWith(p + "/")
    );
    if (isPublic) return NextResponse.next();

    const hasSession = request.cookies.has("JSESSIONID");
    if (!hasSession) {
        const loginUrl = request.nextUrl.clone();
        // 2. Ubah juga path tujuan redirect ke sini
        loginUrl.pathname = "/auth/login";
        return NextResponse.redirect(loginUrl);
    }

    return NextResponse.next();
}

export const config = {
    matcher: ["/((?!_next/static|_next/image|favicon.ico).*)"],
};