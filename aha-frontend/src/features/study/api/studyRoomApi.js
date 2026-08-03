import axiosInstance from "../../../common/api/axiosInstance.js";

/**
 * Spring Data Page 기반 스터디룸 목록 응답.
 *
 * @typedef {Object} StudyRoomPageResponse
 * @property {Array<Object>} content
 * @property {number} page
 * @property {number} size
 * @property {number} totalElements
 * @property {number} totalPages
 * @property {boolean} first
 * @property {boolean} last
 */

const getApiData = (response) =>
    response?.data?.data ?? response?.data ?? response;

const normalizeError = (error) => {
    const responseData = error.response?.data;

    error.status = error.response?.status ?? error.status;
    error.errorCode =
        responseData?.errorCode ||
        responseData?.code ||
        error.errorCode ||
        error.code;
    error.data = responseData?.data ?? error.data;

    return error;
};

/**
 * @returns {Promise<StudyRoomPageResponse>}
 */
export const getStudyRooms = async ({
    examVersionId,
    status = "WAITING",
    sortType = "LATEST",
    page = 0,
    size = 10,
}) => {
    try {
        const response = await axiosInstance.get("/api/v1/study-rooms", {
            params: {
                examVersionId,
                status,
                sortType,
                page,
                size,
            },
        });

        return getApiData(response);
    } catch (error) {
        throw normalizeError(error);
    }
};

export const createStudyRoom = async (request) => {
    try {
        const response = await axiosInstance.post(
            "/api/v1/study-rooms",
            request
        );

        return getApiData(response);
    } catch (error) {
        throw normalizeError(error);
    }
};
