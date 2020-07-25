import React, {Component} from 'react';
import Parent from '../Common/ParentPage';
import Box from '@material-ui/core/Box';
import Button from '@material-ui/core/Button';
import Form from 'react-bootstrap/Form'
import Row from 'react-bootstrap/Row';
import Col from 'react-bootstrap/Col';
import 'bootstrap/dist/css/bootstrap.css';
import BootStrapAPI from '../Apis/BootStrapAPI';
import NodeAPI from '../Apis/NodeAPI';

const styles = {
    container: {
        width: '60%',
        marginLeft: '20%',
        marginRight: '20%',
    },
    headingArea: {
        float: 'center',
        width: '60%',
        marginLeft: '20%',
        marginRight: '20%',
        marginBottom: '10%'
    },
    heading: {
        textAlign: 'center'
    },
    resultArea: {

    }
};

class NodeDistributionPage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            fileName: '',
            fileList: null,
            nodes: [],
            error: null,
            resultSet: null
        };

        this.handleSearch = this.handleSearch.bind(this);
        this.handleUserInput = this.handleUserInput.bind(this);
    }

    handleErrorResponses(error) {
        this.setState({error: error})
    }


    handleSearch(event) {
        event.preventDefault();
        const nodes = this.state.nodes;
        var nodeCount = nodes.length;
        // Pick random node from list
        if (nodeCount > 0) {
            var randomNodeIndex = Math.floor(Math.random() * ((nodeCount - 1) - 0 + 1)) + 0;
            var node = nodes[randomNodeIndex];
            // Sending the search request to the node
            new NodeAPI().searchFile(node, this.state.fileName).then((response) => {
                this.setState({resultSet: response.data.node})
            }).catch((error) => {
                this.handleErrorResponses(error);
            });
        } else {
            var error = {response: {data: {error: "No nodes available in the network."}}};
            this.handleErrorResponses(error);
        }

        // this.state.nodes.map((node) => {
        //     window.alert(node.host)
        // });

        // TODO: List the returned file list in a table
    }

    handleUserInput(event) {
        const target = event.target;
        const name = target.name;
        const value = target.value;
        this.setState({[name]: value});
    }

    componentDidMount() {
        this.retrieveAllNodes();
    }

    retrieveAllNodes() {
        new BootStrapAPI().getAllNodes().then((response) => {
            console.log(response.data.nodes);
            const nodes = response.data.nodes;
            this.setState({nodes: nodes});
        }).catch((error) => {
            this.handleErrorResponses(error);
        });
    }

    isSubmissionInValid() {
        const {fileName} = this.state;
        if (fileName === '') {
            return true;
        }
        return false;
    }

    renderNodeDistributionContent() {
        return (
            <Box style={styles.container}>
                <Box style={styles.headingArea}>
                    <h2 style={styles.heading}><b>NODE DISTRIBUTION</b></h2>
                </Box>
                
                <Box style={styles.resultArea}>

                </Box>
            </Box>);
    }

    render() {
        return (<Parent data={this.renderNodeDistributionContent()} error={this.state.error}/>);
    }
}

export default (NodeDistributionPage);