import Axios from 'axios';

export default class NodeAPI {

    getHTTPClient(node) {

        let httpClient = Axios.create({
            baseURL: `http://${node.host}:${node.port + 100}/fileAPI`,
            timeout: 30000,
        });
        httpClient.defaults.headers.post['Content-Type'] = 'application/json';
        httpClient.interceptors.response.use(function (response) {
            return response;
        }, function (error) {
            console.log(error);
            return Promise.reject(error);
        });
        return httpClient;
    }

    searchFile(node, file) {
        return this.getHTTPClient(node).get(`/searchFile/${file}`);
    }

    retrieveFile(node, file) {
        return this.getHTTPClient(node).get(`/retrieveFile/${file}`);
    }

    retrieveAllFiles(node) {
        return this.getHTTPClient(node).get(`/retrieveAllFiles`);
    }
}
