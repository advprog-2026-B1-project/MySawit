// Delivery types and pure-dummy API layer (no backend calls)

export interface Delivery {
    id: number;
    driver: { id: number; nama: string; username: string };
    mandor: { id: number; nama: string; username: string };
    hasilPanen: { id: number; kilogram: number; tanggalPanen: string; kebun: { namaKebun: string } };
    status: "Memuat" | "Mengirim" | "Tiba";
    mandorDecision: "Pending" | "Approved" | "Rejected";
    adminDecision: "Pending" | "Approved" | "Rejected" | "PartiallyApproved";
    acknowledgedKg?: number;
    mandorRejectionReason?: string;
    adminRejectionReason?: string;
    arrivedAt?: string;
    mandorDecidedAt?: string;
    adminDecidedAt?: string;
    createdAt: string;
    updatedAt: string;
}

import { DUMMY_DELIVERIES, DUMMY_DRIVERS, DUMMY_HARVESTS } from "./dummyData";

// Re-export dummy sets so pages can import them directly
export { DUMMY_DELIVERIES, DUMMY_DRIVERS, DUMMY_HARVESTS };

// ── Simulated delay helper ──────────────────────────────────────────
const wait = (ms = 300) => new Promise(resolve => setTimeout(resolve, ms));

// ── In-memory mutable copy so actions persist in-session ────────────
let deliveries = [...DUMMY_DELIVERIES];
let nextId = 100;

/** Reset to original dummy state (useful for testing) */
export function resetDummyState() {
    deliveries = [...DUMMY_DELIVERIES];
    nextId = 100;
}

// ── Pure-frontend API (no backend dependency) ───────────────────────
export const deliveryApi = {

    async createDelivery(mandorId: number, data: { driverId: number; hasilPanenId: number }): Promise<Delivery> {
        await wait();
        const driver = DUMMY_DRIVERS.find(d => d.id === data.driverId);
        const harvest = DUMMY_HARVESTS.find(h => h.id === data.hasilPanenId);
        if (!driver || !harvest) throw new Error("Data tidak ditemukan");

        const newDelivery: Delivery = {
            id: nextId++,
            driver: { id: driver.id, nama: driver.nama, username: driver.nama.toLowerCase().replace(/\s/g, "_") },
            mandor: { id: mandorId, nama: "Pak Harto", username: "harto_mandor" },
            hasilPanen: { id: harvest.id, kilogram: harvest.kilogram, tanggalPanen: harvest.tanggalPanen, kebun: harvest.kebun },
            status: "Memuat",
            mandorDecision: "Pending",
            adminDecision: "Pending",
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString(),
        };
        deliveries.unshift(newDelivery);
        return newDelivery;
    },

    async updateStatus(deliveryId: number, _driverId: number, data: { status: "Memuat" | "Mengirim" | "Tiba" }): Promise<Delivery> {
        await wait();
        const d = deliveries.find(x => x.id === deliveryId);
        if (!d) throw new Error("Pengiriman tidak ditemukan");
        d.status = data.status;
        d.updatedAt = new Date().toISOString();
        if (data.status === "Tiba") d.arrivedAt = new Date().toISOString();
        return { ...d };
    },

    async decideByMandor(deliveryId: number, _mandorId: number, data: { decision: "Approved" | "Rejected"; rejectionReason?: string }): Promise<Delivery> {
        await wait();
        const d = deliveries.find(x => x.id === deliveryId);
        if (!d) throw new Error("Pengiriman tidak ditemukan");
        d.mandorDecision = data.decision;
        d.mandorDecidedAt = new Date().toISOString();
        d.updatedAt = new Date().toISOString();
        if (data.rejectionReason) d.mandorRejectionReason = data.rejectionReason;
        return { ...d };
    },

    async decideByAdmin(deliveryId: number, data: { decision: "Approved" | "Rejected" | "PartiallyApproved"; rejectionReason?: string; acknowledgedKg?: number }): Promise<Delivery> {
        await wait();
        const d = deliveries.find(x => x.id === deliveryId);
        if (!d) throw new Error("Pengiriman tidak ditemukan");
        d.adminDecision = data.decision;
        d.adminDecidedAt = new Date().toISOString();
        d.updatedAt = new Date().toISOString();
        if (data.rejectionReason) d.adminRejectionReason = data.rejectionReason;
        if (data.acknowledgedKg !== undefined) d.acknowledgedKg = data.acknowledgedKg;
        return { ...d };
    },

    async getByDriver(driverId: number): Promise<Delivery[]> {
        await wait();
        return deliveries.filter(d => d.driver.id === driverId);
    },

    async getMandorOngoing(mandorId: number): Promise<Delivery[]> {
        await wait();
        return deliveries.filter(d => d.mandor.id === mandorId && d.mandorDecision === "Pending");
    },

    async getMandorHistory(mandorId: number): Promise<Delivery[]> {
        await wait();
        return deliveries.filter(d => d.mandor.id === mandorId && d.mandorDecision !== "Pending");
    },

    async getAdminReady(): Promise<Delivery[]> {
        await wait();
        return deliveries.filter(d => d.mandorDecision === "Approved" && d.adminDecision === "Pending");
    },

    async getDetail(deliveryId: number): Promise<Delivery> {
        await wait();
        const found = deliveries.find(d => d.id === deliveryId);
        if (!found) throw new Error("Pengiriman tidak ditemukan");
        return { ...found };
    },
};
