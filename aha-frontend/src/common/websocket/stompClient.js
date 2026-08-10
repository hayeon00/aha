import { Client } from "@stomp/stompjs";
import { getAccessToken } from "../../features/auth/store/authTokenStore.js";

const API_BASE_URL =
    import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

const getBrokerUrl = () => {
    const baseUrl = API_BASE_URL.replace(/\/$/, "");

    if (baseUrl.startsWith("https://")) {
        return `${baseUrl.replace("https://", "wss://")}/ws`;
    }

    return `${baseUrl.replace("http://", "ws://")}/ws`;
};

export const createStompClient = () =>
    new Client({
        brokerURL: getBrokerUrl(),
        beforeConnect: (client) => {
            const accessToken = getAccessToken();

            client.connectHeaders = accessToken
                ? { Authorization: `Bearer ${accessToken}` }
                : {};
        },
        reconnectDelay: 3000,
        heartbeatIncoming: 10000,
        heartbeatOutgoing: 10000,
    });
