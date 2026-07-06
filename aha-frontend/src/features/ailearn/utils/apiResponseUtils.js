export const getApiData = (response) => {
    if (!response) {
        return null;
    }

    if (response.data?.data !== undefined) {
        return response.data.data;
    }

    if (response.data !== undefined) {
        return response.data;
    }

    return response;
};